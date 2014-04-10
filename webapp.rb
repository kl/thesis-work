
require 'sinatra'
require 'json'

set :server, 'thin'
set :port, 9001
set :bind, '0.0.0.0'

set :messages, []
set :current_message, nil

get "/android" do
  erb :main
end

post "/android" do
  message = params[:data]

  settings.messages << message 
  settings.current_message = message
end

get "/appdata" do
  content_type :json

  message = settings.current_message
  settings.current_message = nil

  {message: message}.to_json
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
    <a href="app://web.android/SCANNER_BARCODE/HTTP_POST" onclick="startPolling()">Barcode</a>
    <a href="app://web.android/SCANNER_QR/HTTP_POST"      onclick="startPolling()">QR</a>
    <a href="app://web.android/SCANNER_PRODUCT/HTTP_POST" onclick="startPolling()">Product</a>
    <a href="app://web.android/SCANNER_ALL/HTTP_POST"     onclick="startPolling()">All</a>
    <a href="app://web.android/CHEESE_GRATER/HTTP_POST"   onclick="startPolling()">Non-existing</a>
  </div>
  <hr/>
  <div id="message_list" />
</div>

<script>
function startPolling() {

  function poll() {
      $.get("/appdata", function(data) {
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
  var pollInterval = setInterval(function() { poll(); }, 1000);
}
</script>
