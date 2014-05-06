
function performPoll(url, delay, callback) {

    function poll() {
        $.get(url, callback);
    }
    callback.interval = setInterval(poll, delay);
}

function startPollingRemote() {
    performPoll("/appdata", 1000, handleGet);
}

function startPollingLocal() {
    performPoll("http://localhost:9999", 1000, handleGet);
}

function handleGet(json) {
    if (json.message === "plugin_not_found") {
        clearInterval(handleGet.interval);
        alert("Plugin not found")
    } else if (json.message === "user_cancel") {
        clearInterval(handleGet.interval);
        alert("User canceled")
    } else if (json.email != null) {
        clearInterval(handleGet.interval);
        $("#message_list").append("<p>" + "Email: " + json.email + "</p>");
    } else if (json.message != null) {
        clearInterval(handleGet.interval);
        $("#message_list").append("<p>" + json.message + "</p>");
    }
}

function speedTestHttpPost() {
    speedTest("/appdata", 10, printTime);
}

function speedTestLocalServer() {
    speedTest("http://localhost:9999", 10, printTime);
}

function speedTest(url, pollMs, callback) {

    var startTime = new Date().getTime(); // unix time

    performPoll(url, pollMs, function success(json) {
        if (json.message === "plugin_not_found") {
            clearInterval(success.interval);
            alert("Plugin not found")
        } else if (json.message != null) {
            clearInterval(success.interval);
            var time = new Date().getTime();
            var timeTaken = time - startTime;
            callback(timeTaken);
        }
    });
}

function printTime(time) {
    $("#message_list").append("<p>" + time + " ms" + "</p>");
}

function speedMeasure(maxTimes) {
    var link = "app://web.android/SPEED_TEST_SYSTEM/HTTP_POST";
    var times = 0;

    function measure() {
        speedTest("/appdata", 10, function(time) {
            console.log(time);
            times += 1;
            if (times < maxTimes) {
                setTimeout(function() {
                    window.location.href = link;
                    measure();
                }, 500);
            }
        });
    }

    // Start measurement
    window.location.href = link;
    measure();
}
