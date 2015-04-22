var playerCounter = 0;

$(document).ready(function() {
    $(".alert, #overlay, #loader").hide();

    $('.login-btn').on('click', function() {
        addPlayerToList($('.login-input').val())
    });

});

function addPlayerToList(playerName) {
    if (playerName == "") {
        openAlert('danger', 'Bitte geben Sie einen Spielernamen an!'); return;
    }

    if (playerCounter == 0) {
        $('#highscore table tbody tr td:first').text(1);
        $('#highscore table tbody tr td:nth-child(2)').text(playerName);
        $('#highscore table tbody tr td:nth-child(3)').text(0);
    } else {
        $('#highscore table tbody:last').append('<tr><td>' + parseInt(playerCounter + 1) + '</td><td>' + playerName + '</td><td>0</td></tr>')
    }

    playerCounter = playerCounter + 1;
}

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