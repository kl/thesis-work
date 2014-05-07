
require 'sinatra/base'
require 'json'
require 'rest_client'

class HttpsPostBox < Sinatra::Base

  set :server, 'thin'
  set :port, 9100
  set :bind, '0.0.0.0'

  JSON_PATH = File.join(settings.root, "android_data.json")

  post "/android" do
    RestClient.post("http://localhost:9000/android", request.body.read)
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
