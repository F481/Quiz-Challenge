/*
 * Funktion aktualisiert die Spielerliste
 * Abhängig von der Spielerzahl wird der
 * Start-Button angepasst
 */
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
		else if((playerCount < 2) && (activeCatalog != "")){
			var buttonStart = window.document.getElementById("buttonStart");
			buttonStart.textContent = "Warte auf weitere Spieler ...";
			buttonStart.disabled = true;
		}		
	}	
}