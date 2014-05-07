
require 'sinatra'
require 'json'
require "pry"

set :server, 'thin'
set :port, 9000
set :bind, '0.0.0.0'

set :messages, []
set :current_message, nil

BARCODE_POST        = "app://web.android/SCANNER_BARCODE/HTTP_POST"
BARCODE_HTTPS_POST  = "app://web.android/SCANNER_BARCODE/HTTPS_POST"
QR_POST             = "app://web.android/SCANNER_QR/HTTP_POST"
PRODUCT_POST        = "app://web.android/SCANNER_PRODUCT/HTTP_POST"
ALL_POST            = "app://web.android/SCANNER_ALL/HTTP_POST"
NONEXISTING         = "app://web.android/CHEESE_GRATER/HTTP_POST"
BARCODE_SERVER      = "app://web.android/SCANNER_BARCODE/HTTP_SERVER"
CONTACT_POST        = "app://web.android/CONTACT_PICKER/HTTP_POST"
SPEED_POST          = "app://web.android/SPEED_TEST/HTTP_POST"
SPEED_SERVER        = "app://web.android/SPEED_TEST/HTTP_SERVER"
SPEED_SYSTEM_POST   = "app://web.android/SPEED_TEST_SYSTEM/HTTP_POST"
SPEED_SYSTEM_SERVER = "app://web.android/SPEED_TEST_SYSTEM/HTTP_SERVER"

JSON_PATH    = File.join(settings.root, "android_data.json")
MEASURE_PATH = File.join(settings.root, "measure_data.json")

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

get "/measure/:what/:times" do
  @what = short_url_to_full(params["what"])
  @times = params[:times]
  erb :measure
end

post "/measure" do
  measure = JSON.parse(request.body.read)
  measure["startTime"] = Time.at(measure["timestamp"].to_i / 1000).strftime("%Y-%m-%d %H:%M:%S")
  measure.delete("timestamp")

  data = JSON.parse(read_measure_file)
  data["measurements"] << measure
  File.write(MEASURE_PATH, JSON.pretty_generate(data))

  status 200
end

helpers do
  def clear_current_message
    File.write(JSON_PATH, {data: nil}.to_json)
  end

  def short_url_to_full(short)
    {"bp" => SPEED_POST,
     "bs" => SPEED_SERVER,
     "sp" => SPEED_SYSTEM_POST,
     "ss" => SPEED_SYSTEM_SERVER
    }[short]
  end

  def read_measure_file
    return File.read(MEASURE_PATH) if File.exist?(MEASURE_PATH)
    base = '{"measurements":[]}'
    File.write(MEASURE_PATH, base)
    base
  end
end

__END__

@@ layout
<html>
  <head>
    <title>Android Integration</title>
    <meta charset="utf-8" />
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js" type="text/javascript"></script>
    <script src="/scripts.js" type="text/javascript"></script>

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
      table#speed_test {
      }
    </style>
  </head>

  <body>
    <%= yield %>
  </body>
</html>

@@ main
<div id="main">
  <div id="links">
    <a href="<%= BARCODE_POST %>"       onclick="startPollingRemote()">Barcode</a>
    <a href="<%= QR_POST %>"            onclick="startPollingRemote()">QR</a>
    <a href="<%= PRODUCT_POST %>"       onclick="startPollingRemote()">Product</a>
    <a href="<%= ALL_POST %>"           onclick="startPollingRemote()">All</a>
    <a href="<%= NONEXISTING %>"        onclick="startPollingRemote()">Non-existing</a>
    <a href="<%= BARCODE_SERVER %>"     onclick="startPollingLocal()">Barcode Server</a>
    <a href="<%= BARCODE_HTTPS_POST %>" onclick="startPollingRemote()">Barcode HTTPS</a>
    <a href="<%= CONTACT_POST %>"       onclick="startPollingRemote()">Contact Picker</a>
  </div>
  <hr/>
  <div id="message_list" />
</div>

@@ speedtest
<div id="main">

  <div id="links">
    <table>
      <tr>
        <th>HTTP Post</th>
        <th>Local server</th>
      </tr>
      <tr>
        <td><a id="pb" href="<%= SPEED_POST %>" onclick="speedTestHttpPost()">Built-in</a></td>
        <td><a id="sb" href="<%= SPEED_SERVER %>" onclick="speedTestLocalServer()">Built-in</a></td>
      </tr>
      <tr>
        <td><a id="ps" href="<%= SPEED_SYSTEM_POST %>" onclick="speedTestHttpPost()">System</a></td>
        <td><a id="ss" href="<%= SPEED_SYSTEM_SERVER %>" onclick="speedTestLocalServer()">System</a></td>
      </tr>
    </table>
  </div>

  <hr/>
  <div id="message_list" />
</div>

@@ measure
<div id="main">
  <div id="links">
    <a href="#" onclick="speedMeasure('<%= @what %>', <%= @times %>, counter)">Start measure</a>
  </div>
  <hr/>
  <div id="message_list" />
</div>



