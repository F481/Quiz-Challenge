/*
 * Funktion fragt via AJAX verfügbare Kataloge vom Server ab
 */
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


/*
 * Funktion empfängt über AJAX verfügbare Kataloge
 */
function ajaxServerCatalogResponse(){

	// States (0 - uninitialized, 1 - open, 2 - sent, 3 - receiving) werden nicht verarbeitet
	console.log("received catalogs");
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


/*
 * Funktion reagiert auf den Klick auf einen Fragen Katalog
 * geklickter Fragenkatalog wird farblich hervorgehoben und mit
 * einer Nachricht via Websocket beim Server als aktiver Katalog hinterlegt
 */
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


/*
 * Funktion hebt geklickten Katalog farblich hervor
 */
function highlichtChoosenCatalog(catalogName){
	
	// wurde vom Spieleiter ein Katalog ausgewählt und ein neuer Spieler
	// loggt sich ein, sieht der neue Spieler nich den aktiven Katalog
	// Problem: Websocket epmfängt CatalogChange (Wahl des aktiven Katalogs) bevor
	// eine Antwort über AJAX mit den verfügbarern Katalogen eintrifft
	// Timeout würde helfen?
	// window.setTimeout(code, 500);
	//
	// Test mit 
	// alert("bla)";
	
	// get all catalogs and set background to default
    var catalogArray = window.document.getElementsByClassName("catalogList");
    for(var i = 0; i < catalogArray.length; i++) {
    	if(catalogArray[i].textContent == catalogName){
    		// hebe den aktiven Katalog vor
    		console.log("bla");
    		catalogArray[i].style.backgroundColor="#ffa500";
    	} else {
    		// setze Farbe bei allen anderen Katalogen zurück
    		catalogArray[i].style.backgroundColor="#f3f3f3";
    	}
    }
}