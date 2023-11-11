(ns chatroom.main
  (:require [chatroom.websocket :as ws]))

(defn -main
  "Start the websocket server."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "CLJ_WEBSERVER_PORT") "3000"))]
    (ws/start-server port)))

(comment 
  (-main)
  (ws/start-server 3000)
  (ws/stop-server)
  (ws/notify-clients "hi from the websocket"))
