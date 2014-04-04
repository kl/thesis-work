
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
    a#main {
      font-size: 55;
    }
    li {
      font-size: 40;
    }
  </style>

</head>
<body><%= yield %></body>
</html>

@@ main
<div id="main">
<p><a id="main" href="salestab.android://CHEESE_GRATER" onclick="startPolling()">Start non-existing plugin</a></p>
<p><a id="main" href="salestab.android://FILE_PICKER" onclick="startPolling()">Start FilePicker plugin</a></p>
<ul id="message_list">
</ul>
</div>

<script>
function startPolling() {

  function poll() {
      $.get("/appdata", function(data) {
        if (data.message == "PLUGIN_NOT_FOUND") {
          clearInterval(pollInterval);
          alert("Plugin not found")
        } else if (data.message != null) {
          $("#message_list").append("<li>" + data.message + "</li>");
          clearInterval(pollInterval);
        }
      }); 
  }
  var pollInterval = setInterval(function() { poll(); }, 1000);
}
</script>
