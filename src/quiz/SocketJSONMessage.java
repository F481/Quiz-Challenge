package quiz;

import org.json.JSONException;
import org.json.JSONObject;

import application.Player;
import application.Quiz;



public class SocketJSONMessage {

	private String JSONString; // enthaelt String, der aus JSON-Objekt mit der Methode toString() erzeugt wird
	
	private int messageType; // Typ der Nachricht, 1 = LoginRequest etc.
	private Object[] messageData = new Object[6]; // Strings, z.B Spielername, Fragen, Antworten, ...
	
	/**
	 * Konstruktor baut JSON-Objekt aus Empfangsdaten (JSONString)
	 * @param JSONString String enthält Loginname, Katalogname
	 * @throws JSONException
	 */
	public SocketJSONMessage(String JSONString) throws JSONException{
		
		System.out.println("buld new socketjsonmessage (socketjsonmessage 25)");
		
		this.JSONString = JSONString;
		
		// lege neues JSON Objekt an
		JSONObject jObject = new JSONObject(this.JSONString);

		// lese messageType aus
		switch(jObject.getInt("messageType")){
			case 1: // LoginRequest
				System.out.println("LoginRequest");
				this.messageType = jObject.getInt("messageType");
				this.messageData[0] = jObject.getString("loginName");
				break;
			case 5: // CatalogChange
				System.out.println("CatalogChange 1: " + jObject.getString("catalogName"));
				this.messageType = jObject.getInt("messageType");
				this.messageData[0] = jObject.getString("catalogName");		
				break;
			case 7: // StartGame
				System.out.println("StartGame: " + jObject.getString("catalogName"));
				this.messageType = jObject.getInt("messageType");
				this.messageData[0] = jObject.getString("catalogName");				
				break;
			case 8: // QuestionRequest
				System.out.println("QuestionRequest");
				this.messageType = jObject.getInt("messageType");
				break;
			case 10: // QuestionAnswered
				System.out.println("QuestionAnswered");
				this.messageType = jObject.getInt("messageType");				
				this.messageData[0] = jObject.getLong("selection");
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * Konstruktor baut JSON-Objekt um dieses an den Client zu senden
	 * @param messageType RFC-MessageType
	 * @param message String mit PlayerID, KatalogName, Fragen, Antworten, etc.
	 * @throws JSONException
	 */
	public SocketJSONMessage(int messageType, Object[] message) throws JSONException{
	
		System.out.println("buld new socketjsonmessage");
		
		this.messageType = messageType;
		this.messageData = message;
		
		// erstelle JSONObject und setze MessageType
		JSONObject jObject = new JSONObject();
		jObject.put("messageType", this.messageType);
		
		// setze MessageData (playerID, Katalogname, etc.)
		switch(this.messageType){
			case 2: // LoginResponseOK
				System.out.println("LoginResponseOK");
				jObject.put("playerID", this.messageData[0]);
				break;
			case 5: // CatalogChange
				System.out.println("CatalogChange 2: " + this.messageData[0]);
				jObject.put("catalogName", this.messageData[0]);
				break;
			case 9: // Quesetion
				System.out.println("Question");
				jObject.put("question", this.messageData[0]);
				jObject.put("answer1", this.messageData[1]);
				jObject.put("answer2", this.messageData[2]);
				jObject.put("answer3", this.messageData[3]);
				jObject.put("answer4", this.messageData[4]);
				jObject.put("timeOut", this.messageData[5]);				
				break;
			case 11: // QuestionResult
				System.out.println("QuestionResult");
				jObject.put("timedOut", this.messageData[0]);
				jObject.put("correct", this.messageData[1]);				
				break;
			case 255: // Error
				System.out.println("Error");				
				jObject.put("fatal", this.messageData[0]);
				jObject.put("errorMessage", this.messageData[1]);				
				break;
			default:
				break;
		}
		
		// konvertiere JSON-Obejt zu String
		this.JSONString = jObject.toString();		
	}
	
	
	public SocketJSONMessage(int messageType) throws JSONException {

		System.out.println("buld new socketjsonmessage");
		
		this.messageType = messageType;
				
		// erstelle JSONObject und setze MessageType
		JSONObject jObject = new JSONObject();
		jObject.put("messageType", messageType);
		
		switch(this.messageType){
			case 6: // PlayerList
				Quiz quiz = Quiz.getInstance();
				// baue Spielerliste
				int playerCounter = 0;
		    	for(Player pTemp : quiz.getPlayerList()){    		
		    		// build string for key player
		    		String playerX = "player" + playerCounter + "Name";
		    		jObject.put(playerX, pTemp.getName());
		    		// build string for key score		    		
		    		String scoreX = "player" + playerCounter + "Score";
		    		String score = Long.toString(pTemp.getScore());
		    		jObject.put(scoreX, score);

		    		playerCounter++;
		    	}
				break;
			case 7: // StartGame
				// nothing to do
				break;
			case 12: // GameOver
				System.out.println("GameOver");
				//jObject.put("isAllOver", this.messageData[0]);
				jObject.put("rank", "1");
				break;				
			default:
				break;				
		}
    	
		// konvertiere JSON-Obejt zu String
		this.JSONString = jObject.toString();
	
	}	
	


	public String getJsonString() {
		return JSONString;
	}

	public int getMessageType() {
		return messageType;
	}

	public Object[] getMessage() {
		return messageData;
	}
}

