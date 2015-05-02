package login;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import error.QuizError;
import application.Quiz;

/**
 * @author dschwenk
 * Servlet implementation class Login
 */
public class login extends HttpServlet {

    /**
     * Default constructor. 
     */
    public login(){
    	System.out.println("Aufruf des Konstruktors");
    }

    // Einsprung ins Servlet durch den servlet-Container um Initialisierungen vorzunehmen
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
        System.out.println("Aufruf von init");
   } 

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String playerName = request.getParameter("inputPlayername");
    	
    	// verify playername
    	if(playerName == null){
    		response.getWriter().println("Fehler: Es wurde kein Name eingegeben");
    	} else if("".equals(playerName)){
    		// empty string
    		response.getWriter().println("Fehler: Es wurde kein Name eingegeben");
    	} else {
    		// name OK - create new Player
        	QuizError playerError = new QuizError();
    		if(Quiz.getInstance().createPlayer(playerName, playerError) == null){
    			// error - could not create player
    			response.getWriter().println("Fehler beim Erstellen des Spielers: " + playerError.getType());
    		} else {
    			// successfully created player
        		RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
        		dispatcher.forward(request, response);    			
    		}
    	}
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
    
}
