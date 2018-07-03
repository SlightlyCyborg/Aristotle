window.onload = function() {
    var player_controls = document.getElementsByClassName('player-control');
    for(var i = 0; i < player_controls.length; i++) {
        var player_control = anchors[i];
        player_control.onclick = function(event) {
            event.preventDefault();
        };
    }
};


