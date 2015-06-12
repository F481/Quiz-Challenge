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
import application.Player;
import application.Question;
import application.Quiz;


@ServerEndpoint("/SocketHandler")
public class SocketHandler {
	
	private Player player;
	
	private Timer curTimeOut;
	
	@OnOpen
	// Ein Client meldet sich an und eröffnet eine neue WebSocket-Verbindung
	public void open(Session session, EndpointConfig conf) {
		
		ConnectionManager.addSession(session);
		
		System.out.println("Öffne Socket mit SessionID: " + session.getId());
		
	}
	
	@OnMessage
	public void receiveTextMessage(Session session, String msg, boolean last) throws IOException{
	
		Quiz quiz = Quiz.getInstance();
		QuizError quizError = new QuizError();
		
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
				this.player = Quiz.getInstance().createPlayer(((String) sMessage.getMessage()[0]), quizError);
				
				// Fehler beim Erstellen des Spielers
				if (quizError.isSet()) {
					System.out.println("Login Error: Code: " + Integer.toString(quizError.getStatus()));
					sendError(session, 1, "Spieler konnte nicht erstellt werden: " + quizError.getDescription());
				}

				// setzte Session ID für Spieler
				this.player.setSessionID(session.getId());
				
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
					sendError(session, 1, "AnswerQuestion fehlgeschlagen: "+quizError.getDescription());
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

		if(player != null){			
			if(player.getId() == 0){ // Spieler war Spielleiter
				System.out.println("remove player (player was gamemaster!)");
				QuizError qError = new QuizError();
				Quiz.getInstance().removePlayer(player, qError);
				if(qError.isSet()){
					System.out.println("Remove Player Error: "+qError.getDescription());
				}
				
				// entferne Session
				ConnectionManager.SessionRemove(session);
				System.out.println("close session");
				
				// sende Spielende an alle Spieler - Broadcast
				sendErrorToAll(1, "Spielleiter hat das Spiel verlassen!");	
				
			} else { // Spieler war kein Spielleiter
				System.out.println("remove player (not gamemaster)");
				QuizError qError = new QuizError();
				Quiz.getInstance().removePlayer(player, qError);
				if(qError.isSet()){
					System.out.println("Remove Player Error: "+qError.getDescription());
					// zu wenige Spieler -> beende Spiel
					// sende Spielende an alle Spieler - Broadcast
					sendErrorToAll(1, "Zu wenige Spieler!");	
				}
				
				// entferne Session
				ConnectionManager.SessionRemove(session);
				System.out.println("close session");				

				// es sind noch genug Spieler ( >= 2) vorhanden, sende aktualisierte Spielerliste an alle verbleibenden Spieler
				sendPlayerList();
			}
		}		
	}
	
	
	/**
	 * Methoden sendet PlayerList (Nachricht mit dem Typ 6) an alle Spieler
	 */
	public void sendPlayerList(){
		
		// sende aktualisierte Spielerliste an alle Spieler - Broadcast		
		Quiz quiz = Quiz.getInstance();
		for(Player pTemp : quiz.getPlayerList()){  
			// hole Sessioninformationen
			int id = Integer.parseInt(pTemp.getSessionID());
			Session s = ConnectionManager.getSession(id);
			System.out.println("sende typ 6 an spieler: " + id);
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
	 * Methoden sendet StartGame (Nachricht mit dem Typ 7) an alle Spieler
	 */
	public void sendStartGame(){

		// sende StartGame an alle Spieler - Broadcast		
		Quiz quiz = Quiz.getInstance();
		for(Player pTemp : quiz.getPlayerList()){  
			// hole Sessioninformationen
			int id = Integer.parseInt(pTemp.getSessionID());
			Session s = ConnectionManager.getSession(id);
			System.out.println("sende typ 7 an spieler: " + id);
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
	 * Methoden sendet GameOver (Nachricht mit dem Typ 12) mit der jeweiligen Platzierung an alle Spieler
	 */
	public void sendGameOver(){
				
		String[] player_Name = new String[6];
		String[] player_SID = new String[6];
		long[] player_Score = new long[6];
		
		// hole Spielerinformationen (Name, SID, Punktezahl)
		int playercount=0;
		Quiz quiz = Quiz.getInstance();	
		for(Player pTemp : quiz.getPlayerList()){  

			player_Name[playercount] = pTemp.getName();
			player_SID[playercount] = pTemp.getSessionID();
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
					String temp_SID = player_SID[j];
					
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
			int id = Integer.parseInt(player_SID[i]);
			Session s = ConnectionManager.getSession(id);
			System.out.println("sende typ 12 an den spieler mit ID: " + id);
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
	 * Funktion um Fehlernachricht an Client zu senden
	 * @param session SessionID des Spieler, an den die Nachricht versendet werden soll
	 * @param fatal gibt an ob der Fehler fatal ist, falls ja wird das Spiel beendet
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
	 * Funktion um Fehlernachricht an alle Clients zu senden
	 * @param fatal ist fatal 1, wird das Spiel beendet
	 * @param message Fehlernachricht, die an den Client versendet wird
	 */
	public static void sendErrorToAll(int fatal, String message){
		// sende Fehlernachricht an alle Sessions (Spieler) - Broadcast
		Quiz quiz = Quiz.getInstance();
		for(Player pTemp : quiz.getPlayerList()){ 
			// get session ID of player
			int id = Integer.parseInt(pTemp.getSessionID());
			Session s = ConnectionManager.getSession(id);
			// send error message
			sendError(s, fatal, message);		
		}		
	}	
}
