var playerCounter = 0;

$(document).ready(function() {
    $(".alert, #overlay, #loader").hide();

    $('.login-btn').on('click', function() {
        addPlayerToList($('.login-input').val())
    });

    $('.start-btn').on('click', function() {
        // TODO
        openAlert('success', '"Spiel starten" Button gedrückt!');
        $('#main').empty();
        $('#main').html("<h2>Frage: Bisch du ein netter Kobold?</h2><p>Ja<br>Nein<br>Vll<br>Selber Kobold</p>")
    });

    $('p.catalog').on('click', function() {
        $('.catalog').css('background-color', 'transparent');
        $(this).css('background-color', 'orangered');
    });

    $('.playerlist').on('click', function() {
        alert($(this).val());
        $('#highscore table tbody:first').prepend($(this).text());
        $(this).remove();
    });

});

function addPlayerToList(playerName) {
    // if playername is emtpy, show an error alert
    if (playerName == "") {
        openAlert('danger', 'Bitte geben Sie einen Spielernamen an!');
        return;
    }

    // we have to add the first player in a special way, because there is already one row with "Keine Spieler!"
    if (playerCounter == 0) {
        $('#highscore table tbody tr td:first').text(1);
        $('#highscore table tbody tr td:nth-child(2)').text(playerName);
        $('#highscore table tbody tr td:nth-child(3)').text(0);
    } else {
        // otherwise we append row for row with the needed information
        $('#highscore table tbody:last').append('<tr class="playerlist"><td>' + parseInt(playerCounter + 1) + '</td><td>' + playerName + '</td><td>0</td></tr>')
    }

    playerCounter = playerCounter + 1;

    if (playerCounter >= 2) {
        $('.start-btn').attr('disabled', false);
    }
}


/* Hilfefunktionen für Alerts */
function showOverlay(show) {
    show ? $("#overlay").show() : $("#overlay").hide();
}

function openAlert(type, text) {
    showOverlay(true);

    switch (type) {
        case "success":
            $(".alert-success p").text(text);
            $(".alert-success").show();
            break;
        case "danger":
            $(".alert-danger p").text(text);
            $(".alert-danger").show();
    }

    $(".alert .close:button").click(function () {
        $(".alert").hide();
        showOverlay(false);
    });
};