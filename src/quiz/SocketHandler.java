package quiz;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;

import error.QuizError;
import application.Catalog;
import application.Player;
import application.Question;
import application.Quiz;


@ServerEndpoint("/SocketHandler")
public class SocketHandler {
	
	Quiz quiz = Quiz.getInstance();
	QuizError quizError = new QuizError();
	
	private Player player;	
	private Timer curTimeOut;
	
	
	
	@OnOpen
	// Ein Client meldet sich an und eröffnet eine neue WebSocket-Verbindung
	public void open(Session session, EndpointConfig conf) throws IOException {
		
		ConnectionManager.addSession(session);
		
		System.out.println("Öffne Socket mit SessionID: " + session.getId());
		
		// sende aktuellen / aktiven Katalog an Spieler
		Catalog catalog = quiz.getCurrentCatalog();
		if (catalog != null){
			String catalogName = catalog.getName();
			try {
				session.getBasicRemote().sendText(new SocketJSONMessage(5, new Object[] { catalogName }).getJsonString());						
			} catch (JSONException e) {
				e.printStackTrace();
			}						
		}
	}
	
	
	
	@OnMessage
	public void receiveTextMessage(Session session, String msg, boolean last) throws IOException{	

		System.out.println("Nachricht empfangen: " + msg);		

		SocketJSONMessage sMessage = null;
		try {
			// erzeuge neues Nachrichtenobjekt und parse JSON-String
			sMessage = new SocketJSONMessage(msg);	
		} catch (JSONException e){
			e.printStackTrace();
			// send error nachticht an client
			sendError(session, 0, "Fehlerhafte Nachricht erhalten!");
		}
		
		System.out.println("Nachricht auswerten: " + msg);
		
		// werte Nachrichtentyp aus
		int type = sMessage.getMessageType();
		System.out.println("type: " + type);
		switch(type){
			case 1: // LoginRequest
				System.out.println("typ 1 empfangen - erstelle  neuen spieler");
				// erzeuge Spieler mit Namen aus Paket
				this.player = quiz.createPlayer(((String) sMessage.getMessage()[0]), quizError);
				
				// Fehler beim Erstellen des Spielers
				if (quizError.isSet()) {
					System.out.println("Login Error: Code: " + Integer.toString(quizError.getStatus()));
					sendError(session, 1, "Login nicht möglich: " + quizError.getDescription());
				} 
				// kein Fehler beim Erstellen des Spielers
				else {
					// setzte Session für Spieler
					this.player.setSession(session);
					
					// anlegen des Spielers war erfolgreich
					try {
						System.out.println("sende LoginResponseOK");
						// sende LoginResponseOK mit Spieler-ID
						session.getBasicRemote().sendText(new SocketJSONMessage(2, new Object[] { player.getId() }).getJsonString());
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						sendError(session, 1, "LoginResponseOK konnte nicht erstellt werden!");
					}
					// sende aktualisierte Spielerliste an alle Spieler
					sendPlayerList();
				}
				break;
			case 5: // CatalogChange
				System.out.println("typ 5 empfangen - setzte aktiven Katalog");				
				// quiz.changeCatalog(player, sMessage.getMessage()[0] + ".xml", quizError);
				quiz.changeCatalog(player, sMessage.getMessage()[0].toString(), quizError);
				// prüfe ob setzten des Katalogs erfolgreich
				if (quizError.isSet()) {
					System.out.println(quizError.getDescription());
					sendError(session, 1, "Katalog konnte nicht ausgewählt werden: " + quizError.getDescription());
					return;
				}
				// sende CatalogChange an alle Clients - Broadcast
				for (int i = 0; i < ConnectionManager.SessionCount(); i++) {
					System.out.println("sende typ 5 an spieler: " + i);
					Session s = ConnectionManager.getSession(i);
					try {
						s.getBasicRemote().sendText(new SocketJSONMessage(5, sMessage.getMessage()).getJsonString());
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}				
				break;
			case 7: // StartGame
				System.out.println("typ 7 empfangen - starte Spiel");
				quiz.startGame(player, quizError);
				if (quizError.isSet()) {
		    		System.out.println("quiz error is set");
					System.out.println(quizError.getDescription());
					sendError(session, 1, "Spiel konnte nicht gestartet werden: "+ quizError.getDescription());
					return;
				}
				// send start game to all players - Broadcast
				System.out.println("Send start game broadcast");				
				sendStartGame();

				// sende Spielerliste an alle Spieler
				sendPlayerList();
				break;
			case 8: // QuestionRequest
				System.out.println("typ 8 empfangen - QuestionRequest, sende Frage an Client");
				// starte Timer für Frage
				curTimeOut = new Timer(player, session);
				// hole Frage für Spieler
				Question question = quiz.requestQuestion(player, curTimeOut, quizError);
				if (quizError.isSet()) {
					System.out.println("Error: " + quizError.getDescription());
					sendError(session, 1, "Konnte Question nicht laden: " + quizError.getDescription());
				} else if (question == null && !quizError.isSet()) {
					// keine weitere Frage - Spielende
					System.out.println("Question ist null");
					if(quiz.setDone(player)){ // das Spiel ist zu ende (aller Spieler haben alle Fragen beantwortet)
						System.out.println("Spiel ende");
						
						// sende GameOver (Nachricht mit MessageTyp 12) mit Platzierung an alle Spieler
						sendGameOver();

						// entferne alle Spieler aus dem Spiel
						for(Player pTemp : quiz.getPlayerList()){
							quiz.removePlayer(pTemp, quizError);
							if(quizError.isSet()){
								System.out.println(quizError.getDescription());
							}
						}
					} else { // keine weiteren Fragen für diesen Spieler, warte auf Spielende
						System.out.println("Spieler ende, warte auf andere Spieler");
					}
				} else { // sende Frage + Antworten an Client
					// baue Antworten-Array
					String[] answers = new String[4];
					int i = 0;
					try {
						for (String s : question.getAnswerList()) {
							answers[i] = s;
							i++;
						}
					} catch (NullPointerException e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					try {
						// senden (Frage mit Antworten + Timeout an Client)
						session.getBasicRemote().sendText(
								new SocketJSONMessage(9, new Object[] { question.getQuestion(),
										answers[0], answers[1], answers[2], answers[3],
										(int) (question.getTimeout()/1000) }).getJsonString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						sendError(session, 0, "Erstellen der Question Message fehlgeschlagen");
					}
				}				
				break;
			case 10: // QuestionAnswered
				// werte Spielerantwort aus
				long rightAnswer = quiz.answerQuestion(player,(long) sMessage.getMessage()[0], quizError);
				if (quizError.isSet()) {
					System.out.println(quizError.getDescription());
					sendError(session, 1, "AnswerQuestion fehlgeschlagen: " + quizError.getDescription());
					return;
				}
				try {
					// sende QuestionResult
					System.out.println("index right answer: " + rightAnswer);
					// Parameter false -> Timeout nicht abgelaufen + Index der richtigen Antwort
					session.getBasicRemote().sendText(new SocketJSONMessage(11, new Object[] { false, rightAnswer }).getJsonString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendError(session, 0, "QuestionResult senden fehlgeschlagen!");
				}
				// sende Spielerliste an alle Spieler
				sendPlayerList();				
				break;	
			default:
				System.out.println("default konnte nicht auswerten");
				break;				
		}		
	}
		
	
	
	@OnError
	public void error(Session session, Throwable t) {
		System.out.println("Fehler beim Öffnen des Sockets: " + t);
	}
	
	
	
	@OnClose
	// Client meldet sich wieder ab
	public void close(Session session, CloseReason reason) {

		// Spieler war bereits eingeloggt
		if(player != null){		
			if(player.getId() == 0){ // Spieler war Spielleiter
				System.out.println("remove player (player was gamemaster!)");
				quiz.removePlayer(player, quizError);
				if(quizError.isSet()){
					System.out.println("Remove Player Error: " + quizError.getDescription());
				}
				
				// entferne Session
				System.out.println("Entferne SessionID: " + session.getId());
				ConnectionManager.SessionRemove(session);
				
				// sende Spielende an alle Spieler - Broadcast
				sendErrorToAll(1, "Spielleiter hat das Spiel verlassen!");	
				
			} else { // Spieler war kein Spielleiter
				System.out.println("remove player (not gamemaster)");
				quiz.removePlayer(player, quizError);
				if(quizError.isSet()){
					System.out.println("Remove Player Error: " + quizError.getDescription());
					// zu wenige Spieler -> beende Spiel
					// sende Spielende an alle Spieler - Broadcast
					sendErrorToAll(1, "Zu wenige Spieler!");	
				}
				
				// entferne Session
				System.out.println("Entferne SessionID: " + session.getId());
				ConnectionManager.SessionRemove(session);

				// es sind noch genug Spieler ( >= 2) vorhanden, sende aktualisierte Spielerliste an alle verbleibenden Spieler
				sendPlayerList();
			}
		} else { // Spieler war nicht eingeloggt
			// entferne Session
			System.out.println("Entferne SessionID: " + session.getId());			
			ConnectionManager.SessionRemove(session);
		}
	}
	
	
	/**
	 * Methode sendet PlayerList (Nachricht mit dem Typ 6) an alle Spieler
	 */
	public void sendPlayerList(){
		
		// sende aktualisierte Spielerliste an alle Spieler - Broadcast		
		for(Player pTemp : quiz.getPlayerList()){  
			// hole Sessioninformationen
			Session s = pTemp.getSession();
			System.out.println("sende typ 6 an spieler: " + s.getId());
			try {
				// baue Nachricht + versende Nachricht
				s.getBasicRemote().sendText(new SocketJSONMessage(6).getJsonString());				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}	
		}
	}


	/**
	 * Methode sendet StartGame (Nachricht mit dem Typ 7) an alle Spieler
	 */
	public void sendStartGame(){

		// sende StartGame an alle Spieler - Broadcast		
		for(Player pTemp : quiz.getPlayerList()){  
			// hole Sessioninformationen
			Session s = pTemp.getSession();
			System.out.println("sende typ 7 an spieler: " + s.getId());
			try {
				// baue Nachricht + versende Nachricht
				s.getBasicRemote().sendText(new SocketJSONMessage(7).getJsonString());				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}	
		}		
	}
	
	
	/**
	 * Methode sendet GameOver (Nachricht mit dem Typ 12) mit der jeweiligen Platzierung an alle Spieler
	 */
	public void sendGameOver(){
				
		String[] player_Name = new String[6];
		Session[] player_SID = new Session[6];
		long[] player_Score = new long[6];
		
		// hole Spielerinformationen (Name, SID, Punktezahl)
		int playercount=0;
		for(Player pTemp : quiz.getPlayerList()){  

			player_Name[playercount] = pTemp.getName();
			player_SID[playercount] = pTemp.getSession();
			player_Score[playercount] = pTemp.getScore();
			playercount++;
		}
		
		// sortiere Arrays nach Punktezahl
		for(int i = playercount; i > 0 ; i--){
			for(int j=0; j<(playercount-1);j++){
				// vergleiche Spielstaende - ist Spielstand des nachfolgender groesser - tausche Plaetze
				if(player_Score[j] < player_Score[j+1]){
					long temp_Score = player_Score[j];
					String temp_playerName = player_Name[j];
					Session temp_SID = player_SID[j];
					
					player_Score[j] = player_Score[j+1];
					player_Name[j] = player_Name[j+1];
					player_SID[j] = player_SID[j+1];
					
					player_Score[j+1] = temp_Score;
					player_Name[j+1] = temp_playerName;
					player_SID[j+1] = temp_SID;
				}
			}
		}

		// sende Platzierung an jeden Spieler
		for(int i=0;i<playercount;i++){
			// hole Sessioninformationen
			Session s = player_SID[i];
			System.out.println("sende typ 12 an den spieler mit ID: " + s.getId());
			try {
				// baue Nachricht + versende Nachricht
				s.getBasicRemote().sendText(new SocketJSONMessage(12, new Object[]{i+1}).getJsonString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}
	}
	

	/**
	 * Methode um Fehlernachricht an Client zu senden
	 * @param session SessionID des Spieler, an den die Nachricht versendet werden soll
	 * @param fatal ist fatal 1, wird das Spiel beendet
	 * @param message gibt die Fehlernachricht an
	 */
	public static void sendError(Session session, int fatal, String message){

		try {
			// build JSON-String and send error message
			session.getBasicRemote().sendText(new SocketJSONMessage(255, new Object[]{fatal, message}).getJsonString());
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Methode um Fehlernachricht an alle Clients zu senden
	 * @param fatal ist fatal 1, wird das Spiel beendet
	 * @param message Fehlernachricht, die an den Client versendet wird
	 */
	public static void sendErrorToAll(int fatal, String message){
		// sende Fehlernachricht an alle Sessions (Spieler) - Broadcast
		Quiz quiz = Quiz.getInstance();
		for(Player pTemp : quiz.getPlayerList()){ 
			// get session of player
			Session s = pTemp.getSession();
			// send error message
			sendError(s, fatal, message);
		}
	}
}
