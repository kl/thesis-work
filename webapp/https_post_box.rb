
require 'sinatra/base'
require 'json'

class HttpsPostBox < Sinatra::Base

  set :server, 'thin'
  set :port, 9100
  set :bind, '0.0.0.0'

  JSON_PATH = File.join(settings.root, "android_data.json")

  post "/android" do
    json = JSON.parse(request.body.read)
    message = json["plugin_result"]

    File.write(JSON_PATH, {data: message}.to_json)
    status 200
  end
end

HttpsPostBox.run! do |server|
  server.ssl = true
  server.ssl_options = {
    cert_chain_file:  File.join(HttpsPostBox.settings.root, "cert/server.cert"),
    private_key_file: File.join(HttpsPostBox.settings.root, "cert/server.key"),
    verify_peer:      false
  }
end
