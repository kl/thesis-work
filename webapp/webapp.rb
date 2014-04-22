
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
  json = JSON.parse(request.body.read)
  message = json["message"]

  File.write(JSON_PATH, {data: message}.to_json)
  status 200
end

get "/appdata" do
  content_type :json

  data = JSON.parse(File.read(JSON_PATH))["data"]
  clear_current_message

  response.headers['Access-Control-Allow-Origin'] = '*'
  {message: data}.to_json
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
    <a href="app://web.android/SCANNER_BARCODE/HTTP_SERVER" onclick="startPollingLocal()">Server test</a>
    <a href="app://web.android/SCANNER_BARCODE/HTTPS_POST"   onclick="startPollingRemote()">Barcode HTTPS</a>
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
    $.get(url, function(data) {
      if (data.message === "PLUGIN_NOT_FOUND") {
        clearInterval(pollInterval);
        alert("Plugin not found")
      } else if (data.message === "USER_CANCEL") {
        clearInterval(pollInterval);
        alert("User canceled")
      } else if (data.message != null) {
        clearInterval(pollInterval);
        $("#message_list").append("<p>" + data.message + "</p>");
      }
    });
  }
  var pollInterval = setInterval(poll, 1000);
}
</script>
