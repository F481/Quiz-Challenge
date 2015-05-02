<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quiz-Challenge</title>
	
	<!-- Scripts -->
	<script src="assets/js/quiz.js" type="text/javascript"></script>
	
    <!-- Bootstrap -->
    <link href="assets/libs/bootstrap-3.3.4-dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="assets/libs/bootstrap-3.3.4-dist/js/bootstrap.min.js"></script>

    <link href="assets/css/style.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Ubuntu' rel='stylesheet' type='text/css'>

	<%@ page import="application.Quiz" %>
	<%@ page import="application.Player" %>

</head>
<body onload="init();">

	<% // erzeuge Spielelogik 
		Quiz quiz = Quiz.getInstance(); 
	%> 

    <header class="page-header">
		<span class="glyphicon glyphicon-tower" aria-hidden="true"></span>
        <span>Quiz-Challenge</span>
    </header>

    <section class="container-fluid">
        <div class="row">
            <div id="main" class="section col-md-8">
                <form action="<%= response.encodeURL("login") %>">
                    <input id="inputPlayername" name="inputPlayername" type="text" class="form-control" placeholder="Spielername" />
                    <button id="buttonLogin" type="submit" class="btn btn-default">
                        Anmelden
                        <span class="glyphicon glyphicon-log-in" aria-hidden="true"></span>
                    </button>
                </form>
                <button id="buttonStart" type="button" class="btn btn-default start-btn" disabled="disabled">
                    Spiel starten
                    <span class="glyphicon glyphicon-menu-right" aria-hidden="true"></span>
                </button>
            </div>
            <div class="container col-md-3 col-md-offset-1">
                <div class="row">
                    <div id="catalogs" class="section col-md-12">
                        <div class="modal-header">
                            <span class="glyphicon glyphicon-book" aria-hidden="true"></span>
                            <span>Catalogs</span>
                        </div>
						<% 
							String[] catalogList = Quiz.getInstance().getCatalogList().keySet().toArray(new String[0]);
							// display available catalogs
							for(int i = 0; i < catalogList.length; i++){
							%>
					           	<div class="catalogList">
						           	<%= catalogList[i] %>
								</div>
				           <% 
				           }
						%>
                    </div>
                </div>
                <div class="row">
                    <div id="highscore" class="section col-md-12">
                        <div class="modal-header">
                            <span class="glyphicon glyphicon-star" aria-hidden="true"></span>
                            <span>Highscore</span>
                        </div>
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <td>#</td>
                                    <td>Player</td>
                                    <td>Score</td>
                                </tr>
                            </thead>
                            <tbody id="tablePlayerlistBody">
								<% Player[] playerList = Quiz.getInstance().getPlayerList().toArray(new Player[0]);
									// build playerlist table
									if(playerList.length == 0){
										%>
											<tr>
												<td>Keine Spieler</td>
												<td></td>
												<td></td>
											</tr>										
										<%
									} // there are players
									else {
										for(int i = 0; i < playerList.length ; i++){
											%>
												<tr>
													<td><%= i+1 %></td>
													<td><%= playerList[i].getName() %></td>
													<td><%= playerList[i].getScore() %></td>
												</tr>
											<%
										}
									}
								%>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <footer>Gruppe Frick, Schwenk, Strohm</footer>
</body>
</html>