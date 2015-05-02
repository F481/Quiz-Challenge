// var playerCounter = 0;


function init(){
	
	// lauftext();
    initCataloglist();

	/*
    var buttonLogin = window.document.getElementById("buttonLogin");
	buttonLogin.addEventListener("click",clickedLogin,true);

    var buttonStart = window.document.getElementById("buttonStart");
    buttonStart.addEventListener("click",clickedStart,true);
    */
}


function initCataloglist() {
    // get catalogs and add event listener
    var catalogArray = document.getElementsByClassName("catalogList");
    for(var i = 0; i < catalogArray.length; i++) {
        catalogArray[i].addEventListener("click", clickedCatalog, true);
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
/*
function clickedLogin(event){

    // get value of input field
	var inputName = window.document.getElementById("inputName");
	var playerName = inputName.value;

    // verify user  name
	if (playerName === ""){
		alert("Es wurde kein Spielername eingegeben!");
	} else {
        updatePlayerList(playerName);
	}
	event.stopPropagation();
}

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

function clickedCatalog(event){
    // get all catalogs and set background to default
    var catalogArray = window.document.getElementsByClassName("catalogList");
    for(var i = 0; i < catalogArray.length; i++) {
        catalogArray[i].style.backgroundColor="#f3f3f3";
    }
    // highlight clicked catalog
    event.target.style.backgroundColor="#ffa500";

    event.stopPropagation();
}

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
