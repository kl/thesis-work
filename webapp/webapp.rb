
require 'sinatra'
require 'json'
require 'thread'
require "pry"

$lock = Mutex.new

set :server, 'thin'
set :port, 9000
set :bind, '0.0.0.0'

set :data, nil

BARCODE_POST        = "app://SCANNER_BARCODE/HTTP_POST"
BARCODE_HTTPS_POST  = "app://SCANNER_BARCODE/HTTPS_POST"
QR_POST             = "app://SCANNER_QR/HTTP_POST"
PRODUCT_POST        = "app://SCANNER_PRODUCT/HTTP_POST"
ALL_POST            = "app://SCANNER_ALL/HTTP_POST"
NONEXISTING         = "app://CHEESE_GRATER/HTTP_POST"
BARCODE_SERVER      = "app://SCANNER_BARCODE/HTTP_SERVER"
CONTACT_POST        = "app://CONTACT_PICKER/HTTP_POST"
SPEED_POST          = "app://SPEED_TEST/HTTP_POST"
SPEED_POST_S        = "app://SPEED_TEST/HTTPS_POST"
SPEED_SERVER        = "app://SPEED_TEST/HTTP_SERVER"
SPEED_SYSTEM_POST   = "app://SPEED_TEST_SYSTEM/HTTP_POST"
SPEED_SYSTEM_POST_S = "app://SPEED_TEST_SYSTEM/HTTPS_POST"
SPEED_SYSTEM_SERVER = "app://SPEED_TEST_SYSTEM/HTTP_SERVER"

MEASURE_PATH = File.join(settings.root, "measure_data.json")

get "/android" do
  clear_data
  erb :main
end

post "/android" do
  set_data(request.body.read)
  status 200
end

get "/appdata" do
  content_type :json
  response.headers['Access-Control-Allow-Origin'] = '*'

  status 200
  read_and_clear_data
end

get "/speedtest" do
  clear_data
  erb :speedtest
end

get "/measure/:what/:times" do
  clear_data
  @times = params[:times]
  @what = short_url_to_full(params["what"])
  @delay = get_delay_between_measures(@what)
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
  def set_data(data)
    $lock.synchronize { settings.data = data }
  end

  def read_and_clear_data
    $lock.synchronize do
      data = settings.data
      settings.data = {data: nil}.to_json
      data
    end
  end

  def clear_data
    $lock.synchronize { settings.data = {data: nil}.to_json }
  end

  def short_url_to_full(short)
    {"bp"  => SPEED_POST,
     "bps" => SPEED_POST_S,
     "bs"  => SPEED_SERVER,
     "sp"  => SPEED_SYSTEM_POST,
     "sps" => SPEED_SYSTEM_POST_S,
     "ss"  => SPEED_SYSTEM_SERVER
    }[short]
  end

  def read_measure_file
    return File.read(MEASURE_PATH) if File.exist?(MEASURE_PATH)
    base = '{"measurements":[]}'
    File.write(MEASURE_PATH, base)
    base
  end

  def get_delay_between_measures(plugin_url)
    plugin_url.split("/").last.include?("SERVER") ? 500 : 100
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
        <th>HTTPS Post</th>
        <th>Local server</th>
      </tr>
      <tr>
        <td><a href="<%= SPEED_POST %>" onclick="speedTestHttpPost()">Built-in</a></td>
        <td><a href="<%= SPEED_POST_S %>" onclick="speedTestHttpsPost()">Built-in</a></td>
        <td><a href="<%= SPEED_SERVER %>" onclick="speedTestLocalServer()">Built-in</a></td>
      </tr>
      <tr>
        <td><a href="<%= SPEED_SYSTEM_POST %>" onclick="speedTestHttpPost()">System</a></td>
        <td><a href="<%= SPEED_SYSTEM_POST_S %>" onclick="speedTestHttpsPost()">System</a></td>
        <td><a href="<%= SPEED_SYSTEM_SERVER %>" onclick="speedTestLocalServer()">System</a></td>
      </tr>
    </table>
  </div>

  <hr/>
  <div id="message_list" />
</div>

@@ measure
<div id="main">
  <div id="links">
    <a href="#" onclick="speedMeasure('<%= @what %>', <%= @times %>, <%= @delay %>, counter)">Start measure</a>
  </div>
  <hr/>
  <div id="message_list" />
</div>



