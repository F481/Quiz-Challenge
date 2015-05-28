// Websocket
var socket;
var readyToSend = false;

// AJAX
var request

// Player
var playerId = -1;

// Kataloge
var activeCatalog = "";


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

    // var buttonStart = window.document.getElementById("buttonStart");
    // buttonStart.addEventListener("click",clickedStart,true);

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
			break;
		case 7: // StartGame
			break;
		case 9: // Question
			break;
		case 11: // Question Result
			break;
		case 12: // GameOver
			break;
		case 255: // Error
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
		
		switch(type){
			case 1: // LoginRequest
			    // get value of input field
				var inputName = window.document.getElementById("inputName");
				var playerName = inputName.value;
		        // LoginRequest with type + playername
				jsonData = JSON.stringify({
					messageType : messageType,
					loginName : playerName
				});

				break;
			case 5: // CatalogChange
				break;
			case 7: // StartGame
				break;
			case 8: // QuestionRequest
				break;
			case 10: // QuestionAnswered
				break;
			default: // unknown type
				console.log("Can't send - unknown message type");
				break;
		}
		// send message
		socket.send(jsonData);
	} 
	// socket not ready ro send
	else {
		alert("Verbindung zum Server wurde noch nicht aufgebaut");
	}
}


function processSuccessfulLogin(){
	
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

	// create AJAX-Request-Object
	request = new XMLHttpRequest();
	
	// Kommunikation mit Server initialisieren
	request.open("GET", "AjaxCatalogServerlet", true);
	
	// Eventhandler registrieren, um auf asynchrone Antwort vom Server reagieren zu können
	request.onreadystatechange = ajaxServerCatalogResponse;
	
	// Anfrage senden
	request.send(null);
	
}

function ajaxServerCatalogResponse(){

	// States (0 - uninitialized, 1 - open, 2 - sent, 3 - receiving) werden nicht verarbeitet
	
	// State 4 - die Antwort des Servers liegt vollständig vor
	if(request.readyState == 4){
		var answer = request.responseXML.getElementsByTagName("catalogName");
		for(var i=0;i < answer.length; i++){
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
    // get all catalogs and set background to default
    var catalogArray = window.document.getElementsByClassName("catalogList");
    for(var i = 0; i < catalogArray.length; i++) {
        catalogArray[i].style.backgroundColor="#f3f3f3";
    }
    // highlight clicked catalog
    event.target.style.backgroundColor="#ffa500";

    // set clicked catalog as active catalog
    activeCatalog = event.target.textContent;
    
    event.stopPropagation();
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
    // verify user  name
	if (playerName === ""){
		alert("Es wurde kein Spielername eingegeben!");
	} else {
		// send LoginRequest
		sendWSMessage(1);
	}	
	event.stopPropagation();
}


/*
function updatePlayerList(playerName){

    // update player lists
    if(document.getElementById("listPlayer1").firstElementChild.innerHTML == "Keine Spieler") {
        // set player properties + add event listener
        document.getElementById("listPlayer1").firstElementChild.innerHTML = "1";
        document.getElementById("listPlayer1").children[1].innerHTML = playerName;
        document.getElementById("listPlayer1").addEventListener("click", clickedPlayer, true);
        // ad cells to row below
        document.getElementById("listPlayer2").innerHTML = "<td></td><td></td><td></td>";
    }
    else if (document.getElementById("listPlayer2").firstElementChild.innerHTML === "") {
        document.getElementById("listPlayer2").firstElementChild.innerHTML = "2";
        document.getElementById("listPlayer2").children[1].innerHTML = playerName;
        document.getElementById("listPlayer2").addEventListener("click", clickedPlayer, true);
        document.getElementById("listPlayer3").innerHTML = "<td></td><td></td><td></td>";
    }
    else if (document.getElementById("listPlayer3").firstElementChild.innerHTML === "") {
        document.getElementById("listPlayer3").firstElementChild.innerHTML = "3";
        document.getElementById("listPlayer3").children[1].innerHTML = playerName;
        document.getElementById("listPlayer3").addEventListener("click", clickedPlayer, true);
        document.getElementById("listPlayer4").innerHTML = "<td></td><td></td><td></td>";
    }
    else if (document.getElementById("listPlayer4").firstElementChild.innerHTML === "") {
        document.getElementById("listPlayer4").firstElementChild.innerHTML = "4";
        document.getElementById("listPlayer4").children[1].innerHTML = playerName;
        document.getElementById("listPlayer4").addEventListener("click", clickedPlayer, true);
    }

    playerCounter = playerCounter + 1;

    // disable login button if player limit is reached
    if (playerCounter >= 4) {
        document.getElementById('buttonLogin').disabled = true;
    }

    // enable start button
    if (playerCounter >= 2) {
        document.getElementById('buttonStart').disabled = false;
    }
}
*/



/*
function clickedStart(event){

    // clean up main div
    document.getElementById("main").innerHTML = "";

    // update main div with question
    document.getElementById("main").innerHTML = "<h2>Frage: Bisch du ein netter Kobold?</h2><p>Ja<br>Nein<br>Vll<br>Selber Kobold</p>";

    event.stopPropagation();
}


function clickedPlayer(event) {

    var firstPlayer = document.getElementById("tablePlayerlistBody").firstChild;

    firstPlayer.parentNode.insertBefore(event.target.parentNode, firstPlayer);
}
*/


