// Websocket
var socket;
var readyToSend = false;

// AJAX
var request;

//Game
var gameRunning = false;

// Player
playerId = -1;
var playerCount = 0;

// Playerliste
var curPlayerList;

// aktiver / ausgewählter Katalog
var activeCatalog = "";

// Question
var curQuestion = "";
var curAnswer1 = "";
var curAnswer2 = "";
var curAnswer3 = "";
var curAnswer4 = "";
var curTimeOut = 0;
var isQuestionActive = false;

// Antwortauswahl
var curSelection = -1;


function init(){

    // lauftext();

    // open websocket
    var url = 'ws:localhost:8080/Quiz-Challenge-Server/SocketHandler';
    socket = new WebSocket(url);

    // event handler websocket
    socket.onopen = function(){
        readyToSend = true;
    };

    socket.onerror = function(event){
        alert("Websocket Error: " + event.data);
    };

    socket.onclose = function(event){
        console.log("Websocket geschlossen: " + event.data);
    };

    socket.onmessage = receiveWSMessage;

    // listener login + start button
    var buttonLogin = window.document.getElementById("buttonLogin");
    buttonLogin.addEventListener("click",clickedLogin,true);

    var buttonStart = window.document.getElementById("buttonStart");
    buttonStart.addEventListener("click",clickedStart,true);

}


function receiveWSMessage(message){
		
	var parsedJSONMessage = JSON.parse(message.data);
	
	console.log("Received message type: " + parsedJSONMessage.messageType);
	
	switch (parsedJSONMessage.messageType){
		case 2: // LoginResponseOK
			console.log("Player ID: " + parsedJSONMessage.playerID);
            playerId = parsedJSONMessage.playerID;
			processSuccessfulLogin();
			break;
		case 5: // CatalogChange
			console.log("Catalog changed: " + parsedJSONMessage.catalogName);
			activeCatalog = parsedJSONMessage.catalogName;
			highlichtChoosenCatalog(activeCatalog);
			break;
		case 6: // PlayerList
			console.log("PlayerList" );
			var playerlist = parsedJSONMessage;
			curPlayerList = playerlist;
			updatePlayerList(playerlist);
			break;
		case 7: // StartGame
			console.log("Startgame");
			gameRunning = true;
			// clear login stuff
			clearLoginDiv();
			showGameDiv();
			// request first question
			sendWSMessage(8);
			break;
		case 9: // Question
			console.log("Question: " + parsedJSONMessage.question);
			curQuestion = parsedJSONMessage.question;
			curAnswer1 = parsedJSONMessage.answer1;
			curAnswer2 = parsedJSONMessage.answer2;
			curAnswer3 = parsedJSONMessage.answer3;
			curAnswer4 = parsedJSONMessage.answer4;
			curTimeOut = parsedJSONMessage.timeOut;
			showQuestion();
			isQuestionActive = true;
			break;
		case 11: // Question Result
			console.log("Correct: " + parsedJSONMessage.correct);
			
			// markiere Spielerauswahl rot
			if(curSelection != -1){
				document.getElementById(curSelection).style.borderColor = "red";
				document.getElementById(curSelection).style.backgroundColor = "#FF0800";				
			}
			
			// markiere korrekte Antworten grüen
			document.getElementById(parsedJSONMessage.correct).style.borderColor = "green";
			document.getElementById(parsedJSONMessage.correct).style.backgroundColor = "#8DB600";

			// -> false -> es können keine Antworten mehr geklickt werden
			isQuestionActive = false;
			
			window.setTimeout(function() {
				// frage nach zwei Sekunden neue Frage an
				sendWSMessage(8);
			}, 2000)			
			break;
		case 12: // GameOver
			console.log("GameOver - Spiel ist zu ende");
			GameOver(parsedJSONMessage);			
			break;
		case 255: // Error
			console.log("Error: " + parsedJSONMessage.errorMessage);			
			if(parsedJSONMessage.fatal == 1){ // Spiel beenden
				alert("Es ist ein fataler Fehler aufgetreten: " + parsedJSONMessage.errorMessage + " Das Spiel wird beendet!");
				// reload page to start new game?
				// startNewGame(); --> function is not implemented yet
				// tue etwas - Seite neu laden, Sppiel neu starten?
			    // clean up main div
			    document.getElementById("main").innerHTML = "<h1>Das Spiel wurde beendet!</h1>";
			} else { // Fehler ist nicht fatal, Spiel wird nicht beendet
				console.log("Warning: " + parsedJSONMessage.errorMessage);
			}
			break;
		default:
			console.log("Received unknown message type");
			break;			
	}
}


function sendWSMessage(type){
	// verify if websocket is ready to send
	if(readyToSend){
		
		var messageType = type.toString();
		var jsonData;
		var selection = curSelection;
		
		switch(type){
			case 1: // LoginRequest
			    // get value of input field
				var inputName = window.document.getElementById("inputName");
				var playerName = inputName.value;
		        // LoginRequest with type + playername
				console.log("send MessageType 1");
				jsonData = JSON.stringify({
					messageType : messageType,
					loginName : playerName
				});
				break;
			case 5: // CatalogChange
				var catalogName = activeCatalog;
		        // CatalogChange with type + catalogname
				console.log("send MessageType 5");
				jsonData = JSON.stringify({
					messageType : messageType,
					catalogName : catalogName
				});
				break;
			case 7: // StartGame
				var catalogName = activeCatalog;
		        // StartGame with type + catalogname
				console.log("send MessageType 7");
				jsonData = JSON.stringify({
					messageType : messageType,
					catalogName : catalogName
				});
				break;
			case 8: // QuestionRequest
				// QuestionRequest with type
				console.log("send MessageType 8");
				jsonData = JSON.stringify({
					messageType : messageType
				});				
				break;
			case 10: // QuestionAnswered
				// QuestionAnswered with type + selected answer		
				console.log("send MessageType 10");				
				jsonData = JSON.stringify({
					messageType : messageType,
					selection : selection
				});				
				break;
			default: // unknown type
				console.log("Can't send - unknown message type");
				break;
		}
		// send message
		socket.send(jsonData);
	} 
	// socket not ready to send
	else {
		alert("Verbindung zum Server wurde noch nicht aufgebaut");
	}
}


function processSuccessfulLogin(){
	
	// remove login button + name input field
	var mainDiv = document.getElementById("main");
	mainDiv.removeChild(document.getElementById("loginForm"));

	// change text of start button
    var buttonStart = window.document.getElementById("buttonStart");
	if(playerId == 0){
		// Spielleiter
		buttonStart.textContent = "Warte auf weitere Spieler ...";
	} else {
		// kein Spielleiter
		buttonStart.textContent = "Warte auf Spielstart ...";
	}
	
	// request catalogs
	requestCatalogs();
}


function requestCatalogs() {
	// prüefe ob Browser AJAX unterstützt
	if (window.XMLHttpRequest){ // code for IE7+, Firefox, Chrome, Opera, Safari
		// create AJAX-Request-Object
		request = new XMLHttpRequest();
		
		// Kommunikation mit Server initialisieren
		request.open("GET", "AjaxCatalogServerlet", true);
		
		// Eventhandler registrieren, um auf asynchrone Antwort vom Server reagieren zu können
		request.onreadystatechange = ajaxServerCatalogResponse;
		
		// Anfrage senden
		request.send(null);		
	} else { // code for IE6, IE5, non AJAX compatible browsers
		alert("Kann Katalog nicht auswählen - Browser unterstützt kein AJAX. Für das Spiel ist IE7+, Firefox, Chrome, Opera, Safari oder ein anderer AJAX-fähriger Browser notwendig!");
	}	
}


function ajaxServerCatalogResponse(){

	// States (0 - uninitialized, 1 - open, 2 - sent, 3 - receiving) werden nicht verarbeitet
	
	// State 4 - die Antwort des Servers liegt vollständig vor
	if(request.readyState == 4){
		var answer = request.responseXML.getElementsByTagName("catalogName");
		for(var i = 0; i < answer.length; i++){
			// erzeuge div mit Text, weise CSS-Klasse hinzu
			var catalogDiv = document.createElement("div");
			catalogDiv.className = "catalogList";
			catalogDiv.textContent = answer[i].firstChild.nodeValue;
			// füge div zum DOM-Baum hinzu + registriere EventListener
			document.getElementById("catalogs").appendChild(catalogDiv);
			catalogDiv.addEventListener("click", clickedCatalog, false);
		}
	}
}


function clickedCatalog(event){
	if((playerId == 0) && (gameRunning == false)){
        // hebe den ausgewählten Katalog hervor
        activeCatalog = event.target.textContent;        
        highlichtChoosenCatalog(activeCatalog);
        
        // send catalog change
        sendWSMessage(5);
        
        // passe start button an falls genügend Spieler angemeldet sind (Text ändern, Start button aktivieren)
		if(playerCount >= 2){
			var buttonStart = window.document.getElementById("buttonStart");
			buttonStart.textContent = "Spiel starten";
			buttonStart.disabled = false;
		}
    }	    
    event.stopPropagation();
}


function highlichtChoosenCatalog(catalogName){
	
	// get all catalogs and set background to default
    var catalogArray = window.document.getElementsByClassName("catalogList");
    for(var i = 0; i < catalogArray.length; i++) {
    	if(catalogArray[i].textContent == catalogName){
    		// hebe den aktiven Katalog vor
    		catalogArray[i].style.backgroundColor="#ffa500";
    	} else {
    		// setze Farbe bei allen anderen Katalogen zurück
    		catalogArray[i].style.backgroundColor="#f3f3f3";
    	}
    }
}


function updatePlayerList(playerlist){

	// get playerlist table
	var table = document.getElementById("highscoreTable").getElementsByTagName("tbody")[0];
	// remove all entries of table
	while (table.firstChild) {
		table.removeChild(table.firstChild);
	}

	playerCount = 0;

	// JSON String
	// message data Playerlist: {"messageType":6,"players":[{"score":"0","player":"dsgffd"},{"score":"0","player":"qwe"}]}
	
	var playerListArray = playerlist.players;	
	var length = playerListArray.length; 
	
	// build table entries
	for(var i=0;i<length;i++){
		var row = table.insertRow();
		var cellRank = row.insertCell();
		cellRank.textContent = i+1;
		var cellPlayer = row.insertCell();
		cellPlayer.textContent = playerListArray[i].playername;
		var cellScore = row.insertCell();
		cellScore.textContent = playerListArray[i].score;
		
		playerCount++;
	}

	if((playerId == 0) && (gameRunning == false)){
		// aktiviere Startbutton wenn genug Spieler + Katalog ausgewählt
		if((playerCount >= 2) && (activeCatalog == "")){
			var buttonStart = window.document.getElementById("buttonStart");
			buttonStart.textContent = "Wähle Katalog um Spiel zu starten";
		}
		else if((playerCount >= 2) && (activeCatalog != "")){
			var buttonStart = window.document.getElementById("buttonStart");
			buttonStart.textContent = "Spiel starten";
			buttonStart.disabled = false;
		}
	}	
}


/*
var begin = 0;

function lauftext() {	
	
	var text = "Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * Quiz-Webprogrammierung * * * * * * * * * * ";
	var end = text.length;

    // cut text at front and append at the end
	document.getElementById("inputLauftext").value = text.substring(begin,end) + text.substring(0,begin);
	begin ++;
	if(begin >= end){ 
 		begin = 0;
	}
	// speed of text - higher -> slower
	window.setTimeout("lauftext()", 150); 
}
*/


function clickedLogin(event){

	var inputName = window.document.getElementById("inputName");
	var playerName = inputName.value;
    // verify user name
	if (playerName === ""){
		alert("Es wurde kein Spielername eingegeben!");
	} else {
		// send LoginRequest
		sendWSMessage(1);
	}	
	event.stopPropagation();
}




function clearLoginDiv(){
    // clean up main div
    document.getElementById("main").innerHTML = "";
}


function clickedStart(event){

    // clean up main div
	clearLoginDiv();

    // send GameStart
    sendWSMessage(7);
    
    event.stopPropagation();
}


function showQuestion(){
	console.log("frage anzeigen");	
	
	document.getElementById("QuestionText").textContent = curQuestion;

	var answerText = [ curAnswer1, curAnswer2, curAnswer3, curAnswer4 ];

	var answers = document.getElementsByClassName("answerDiv");
	for (var i = 0; i < 4; i++) {
		answers[i].style.borderColor = "black";
		answers[i].style.backgroundColor = "white";
		answers[i].textContent = answerText[i];
	}
	document.getElementById("timeOut").textContent = "Time Out: " + curTimeOut	+ " Sekunden";
}


function showGameDiv(){
	
	var mainDiv = document.getElementById("main");
	
	// div (container) für Überschrift (Fragenkatalog), Frage, Antworten, Timer
	var questDiv = document.createElement("div");
	questDiv.id = "questDiv";
	
	// Überschrift Fragenkatalog
	var title = document.createElement("h3");
	title.id = "GameDivTitle";
	title.textContent = "Fragekatalog: " + activeCatalog;
	
	// div für Frage
	var question = document.createElement("div");
	question.id = "QuestionText";
	question.style.fontSize = "16px";
	
	questDiv.appendChild(title);
	questDiv.appendChild(question);

	var answers = [];

	for (var i = 0; i < 4; i++) {
		answers[i] = document.createElement("div");
		answers[i].className = "answerDiv";
		answers[i].id = i;

		answers[i].addEventListener("click", function(event) {
			if (isQuestionActive) {
				// lese Antwort auswahl aus
				curSelection = event.target.id;
				console.log("clicked answer: " + event.target.id);
				// sende QuestionAnwswered
				sendWSMessage(10);
			}
		}, false);
		// füge Frage dem div hinzu ("zeige Frage an")
		questDiv.appendChild(answers[i]);
	}
	
	// timout
	var timeOut = document.createElement("p");
	timeOut.id = "timeOut";
	
	// füge Time out dem div hinz
	questDiv.appendChild(timeOut);
	
	// füge für Überschrift (Fragenkatalog), Frage, Antworten, Timer dem main div hinzu
	mainDiv.appendChild(questDiv);
}


/*
function clickedPlayer(event) {

    var firstPlayer = document.getElementById("tablePlayerlistBody").firstChild;

    firstPlayer.parentNode.insertBefore(event.target.parentNode, firstPlayer);
}
*/


function GameOver(parsedJSONMessage) {
	var questDiv = document.getElementById("questDiv");
	while (questDiv.firstChild) {
		questDiv.removeChild(questDiv.firstChild);
	}
	var title = document.createElement("h3");
	title.textContent = "Game Over!";
	questDiv.appendChild(title);
	
	rank = parsedJSONMessage.rank;
	if(rank == 1){
		// Spiel gewonnen
		alert("Glückwünsch. Sie haben das Spiel gewonnen!");
	} else {
		// Spiel nicht gewonnen
		alert("Glückwünsch. Sie wurden " + rank + ".!");
	}
	
	// tue etwas - Seite neu laden, Sppiel neu starten?
    // clean up main div
    document.getElementById("main").innerHTML = "<h1>Das Spiel ist zu Ende!</h1>";
	
}