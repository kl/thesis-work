
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
            var timeTaken = (new Date().getTime()) - startTime;
            clearInterval(success.interval);
            callback(timeTaken);
        }
    });
}

function printTime(time) {
    $("#message_list").append("<p>" + time + " ms" + "</p>");
}

function speedMeasure(link, maxTimes, callback) {
    var measure = {};
    measure.pluginLink = link;
    measure.times = maxTimes;
    measure.pollUrl = getPollUrl(link);
    measure.timestamp = new Date().getTime();
    measure.values = [];
    console.log("Starting measurement\nlink: " + measure.pluginLink + "\ntimes: " + measure.times + "\npoll url: " + measure.pollUrl);

    var times = 0;
    function start() {
        speedTest(measure.pollUrl, 10, function(time) {
            if ($.type(callback) !== "undefined") callback(time);
            measure.values.push(time);
            times += 1;

            if (times < measure.times) {
                setTimeout(function() {
                    window.location.href = measure.pluginLink;
                    start();
                }, 500);
            } else {
                $.post("/measure", JSON.stringify(measure), function() {
                    console.log("measure success");
                    if ($.type(callback) !== "undefined") callback("done");
                });
            }
        });
    }

    // Start measurement
    window.location.href = measure.pluginLink;
    start();
}

function getPollUrl(link) {
    var parts = link.split("/");
    var last = parts[parts.length-1];

    if (last.indexOf("SERVER") === -1) {
        return "/appdata";
    } else {
        return "http://localhost:9999";
    }
}

function counter(time) {
    (time === "done") ? $("#message_list").append("*") : $("#message_list").append(". ");
}
