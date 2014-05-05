
require 'sinatra'
require 'json'

set :server, 'thin'
set :port, 9000
set :bind, '0.0.0.0'

set :messages, []
set :current_message, nil

JSON_PATH = File.join(settings.root, "android_data.json")

get "/android" do
  clear_current_message
  erb :main
end

post "/android" do
  File.write(JSON_PATH, request.body.read)
  status 200
end

get "/appdata" do
  content_type :json

  json = File.read(JSON_PATH)
  clear_current_message

  response.headers['Access-Control-Allow-Origin'] = '*'
  json
end

get "/speedtest" do
  erb :speedtest
end

helpers do
  def clear_current_message
    File.write(JSON_PATH, {data: nil}.to_json)
  end
end

__END__

@@ layout
<html>
<head>
  <title>Android Integration</title>
  <meta charset="utf-8" />
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>

  <style>
    div#main {
      width: 700px;
      margin-top: 10%;
      margin-left: auto;
      margin-right: auto;
    } 
    div#links a {
      font-size: 30;
      margin-right: 10;
    }
    ul#message_list {
      list-style-type: none;
    }
    div#message_list {
      font-size: 30;
    }
  </style>

</head>
<body><%= yield %></body>
</html>

@@ main
<div id="main">
  <div id="links">
    <a href="app://web.android/SCANNER_BARCODE/HTTP_POST"   onclick="startPollingRemote()">Barcode</a>
    <a href="app://web.android/SCANNER_QR/HTTP_POST"        onclick="startPollingRemote()">QR</a>
    <a href="app://web.android/SCANNER_PRODUCT/HTTP_POST"   onclick="startPollingRemote()">Product</a>
    <a href="app://web.android/SCANNER_ALL/HTTP_POST"       onclick="startPollingRemote()">All</a>
    <a href="app://web.android/CHEESE_GRATER/HTTP_POST"     onclick="startPollingRemote()">Non-existing</a>
    <a href="app://web.android/SCANNER_BARCODE/HTTP_SERVER" onclick="startPollingLocal()">Barcode Server</a>
    <a href="app://web.android/SCANNER_BARCODE/HTTPS_POST"  onclick="startPollingRemote()">Barcode HTTPS</a>
    <a href="app://web.android/CONTACT_PICKER/HTTP_POST"    onclick="startPollingRemote()">Contact Picker</a>
  </div>
  <hr/>
  <div id="message_list" />
</div>

<script>
function startPollingRemote() {                         
  performPoll("/appdata");
}

function startPollingLocal() {                         
  performPoll("http://localhost:9999");
}

function performPoll(url) {

  function poll() {
    $.get(url, function(json) {
      if (json.message === "plugin_not_found") {
        clearInterval(pollInterval);
        alert("Plugin not found")
      } else if (json.message === "user_cancel") {
        clearInterval(pollInterval);
        alert("User canceled")
      } else if (json.email != null) {
        clearInterval(pollInterval);
        $("#message_list").append("<p>" + "Email: " + json.email + "</p>");
      } else if (json.message != null) {
        clearInterval(pollInterval);
        $("#message_list").append("<p>" + json.message + "</p>");
      }
    });
  }
  var pollInterval = setInterval(poll, 1000);
}
</script>

@@ speedtest
<div id="main">
  <div id="links">
    <a href="app://web.android/SPEED_TEST/HTTP_POST" onclick="speedTestPollRemote('/appdata', 10)">Built-in controller - HTTP post</a>
    <a href="app://web.android/SPEED_TEST/HTTP_SERVER" onclick="speedTestPollRemote('http://localhost:9999', 10)">Built-in controller - Local server</a>
  </div>
  <div id="message_list" />
</div>

<script>

function speedTestPollRemote(url, pollMs) {

  var startTime = new Date().getTime(); // unix time

  function poll() {
    $.get(url, function(json) {
      if (json.message != null) {
        clearInterval(pollInterval);
        var time = new Date().getTime();
        var timeTaken = time - startTime;
        $("#message_list").append("<p>" + timeTaken + " ms" + "</p>");
      }
    });
  }
  var pollInterval = setInterval(poll, pollMs);
}

</script>















































