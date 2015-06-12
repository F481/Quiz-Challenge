package quiz;

import java.io.IOException;
import java.util.TimerTask;

import javax.websocket.Session;

import org.json.JSONException;

import error.QuizError;
import application.Player;
import application.Quiz;



/*
 * Klasse für das Timout der Fragen
 * Ist Timout einer Frage abgelaufen wird eine Nachricht
 * mit den korrekten Antworten an den Client gesendet
 */
public class Timer extends TimerTask {
	
	private Session tSession;
	
	private Player tPlayer;	
	
	public Timer(Player _tPlayer, Session _tSession) {
		tPlayer = _tPlayer;
		tSession = _tSession;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Time Out!");
		try {
			QuizError qError = new QuizError();
			try {
				// erstelle Timoutnachricht und sende diese an den Client
				tSession.getBasicRemote().sendText(
						// Parameter true -> Timeout abgelaufen + Index der richtigen Antwort
						new SocketJSONMessage(11, new Object[] { true, Quiz.getInstance().answerQuestion(tPlayer, -1, qError) })
								.getJsonString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				SocketHandler.sendError(tSession, 0, "Erstellen der TimeOut Message fehlgeschlagen!");
				e.printStackTrace();
			}
			if(qError.isSet()){
				System.out.println("TimeOut Answer Error: "+qError.getDescription());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}