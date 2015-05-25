package quiz;

import org.json.JSONException;
import org.json.JSONObject;



public class SocketJSONMessage {

	private int messageType;
	private String JSONString;
	private Object[] messageData = new Object[6];
	
	/**
	 * Konstruktor baut JSON-Objekt aus Empfangsdaten (JSONString)
	 * @param JSONString String enthält Loginname, Katalogname
	 * @throws JSONException
	 */
	public SocketJSONMessage(String JSONString) throws JSONException{
		
		System.out.println("buld new socketjsonmessage");
		
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
				break;
			case 7: // StartGame
				break;
			case 10: // QuestionAnswered
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
			case 5: //
				break;
			case 7: //
				break;
			case 9: //
				break;
			case 11: //
				break;
			case 12: // GameOver
				break;
			case 255: // Error
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

