
function startPollingRemote() {
    performPoll("/appdata", 500, handleGet);
}

function startPollingLocal() {
    performPoll("http://localhost:9999", 500, handleGet);
}

function performPoll(url, delay, callback) {

    function poll() {
        $.ajax({
            dataType: "json",
            url: url,
            success: function(result) {
                performPoll.timeout = setTimeout(poll, delay);
                callback(result, performPoll.timeout);
            },
            error: function() {
                performPoll.timeout = setTimeout(poll, delay);
            }
        });
    }
    poll();
}

function handleGet(json, pollTimeout) {
    if (json.message === "plugin_not_found") {
        clearTimeout(pollTimeout);
        alert("Plugin not found")
    } else if (json.message === "user_cancel") {
        clearTimeout(pollTimeout);
        alert("User canceled")
    } else if (json.email != null) {
        clearTimeout(pollTimeout);
        $("#message_list").append("<p>" + "Email: " + json.email + "</p>");
    } else if (json.message != null) {
        clearTimeout(pollTimeout);
        $("#message_list").append("<p>" + json.message + "</p>");
    }
}

function speedTestHttpPost() {
    speedTest("/appdata", 10, printTime);
}

function speedTestHttpsPost() {
    speedTestHttpPost();
}

function speedTestLocalServer() {
    speedTest("http://localhost:9999", 10, printTime);
}

function speedTest(url, pollMs, callback) {

    var startTime = new Date().getTime(); // unix time

    performPoll(url, pollMs, function success(json, pollTimeout) {
        if (json.message === "plugin_not_found") {
            clearTimeout(pollTimeout);
            alert("Plugin not found")
        } else if (json.message != null) {
            clearTimeout(pollTimeout);
            var timeTaken = (new Date().getTime()) - startTime;
            callback(timeTaken);
        }
    });
}

function printTime(time) {
    $("#message_list").append("<p>" + time + " ms" + "</p>");
}

function speedMeasure(link, maxTimes, delay, callback) {
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
                }, delay);
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
    (time === "done") ? $("#message_list").append("😃 ") : $("#message_list").append("😸 ");
}
