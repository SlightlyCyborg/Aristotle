var tag = document.createElement('script');

tag.src = "https://www.youtube.com/iframe_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

$(".thumbnail").click(function (){
    video_id = $(this).data("video-id");
    $(this).remove();
    $("#video-" + video_id).parent().show();

});

var players = {};
function onYouTubePlayerAPIReady() {

    $(".thumbnail").each(function(){
        video_id = $(this).data("video-id");
        w = $(this).width();
        h = $(this).height();
        player = new YT.Player('video-' + video_id, {
            height: h,
            width: w,
            videoId: video_id,
            events: {
                'onReady': onPlayerReady,
                'onStateChange': onPlayerStateChange
            }
        });
        players[video_id] = player;
    });
}

var currently_playing = null;
function onPlayerReady(event){
    //event.target.playVideo();
    wrapper = $(event.target.getIframe()).parent();
    video_id = wrapper.data("video-id");
    console.log(video_id);
    $("#img-" + video_id).hide();
    $("#video-wrapper-" + video_id).show();
    $(".player-control-for-" + video_id).click(function(e){
        e.preventDefault();
        this_video_id = $(this).data("video-id");
        this_wrapper = $("#video-wrapper-" + this_video_id).show();
        time = $(this).data("time");
        players[this_video_id].seekTo(time, true);
        if (currently_playing != null) {
            players[currently_playing].pauseVideo();
        }
        players[this_video_id].playVideo();
        currently_playing = this_video_id;
        $('html, body').animate({
            scrollTop: this_wrapper.offset().top
        }, 1000);
    });
    if(to_execute.length > 0){
        for (var i = 0; i < to_execute.length; i++){
            if (to_execute[i].dataset.videoId == video_id){
                console.log("clicking");
                $(to_execute[0]).click();
                to_execute = [];
            }
        }
    }
};

function onPlayerStateChange(){
}
