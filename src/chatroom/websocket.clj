(ns chatroom.websocket
  (:require [chatroom.utils :as utils]
            [chatroom.clients :as clients]
            [chatroom.components.chat :as chat]
            [chatroom.components.login :as login]
            [chatroom.components.avatar :as ava]
            [chatroom.db :as db]
            [compojure.core :refer [defroutes GET POST routes]]
            [org.httpkit.server :as server]
            [hiccup.core :as hiccup]
            [ring.middleware.params :as rmp]
            [ring.middleware.keyword-params :as rmkp]
            [ring.middleware.resource :as rmr]
            [cheshire.core :as cheshire]
            [clojure.data.json :as json]
            [clojure.string :as s]))


(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn notify-clients [msg]
  (doseq [[_ channel] @clients/clients]
    (server/send! channel msg)))

(defn on-chat-msg!
  " Add a new message to history and update the msg color state."
  [author msg]
  (reset! chat/msg-box-color-switch (not @chat/msg-box-color-switch))
  (when (not (get @ava/author->avatar author))
    (swap! ava/author->avatar assoc author (chat/random-avatar)))
  (swap! db/msg-log conj {:author author :body msg}))

;; see https://github.com/http-kit/http-kit/blob/master/src/org/httpkit/server.clj
(defn ws-connect-handler [ring-req]
  (if-not (:websocket? ring-req)
    {:status 200 :body "Welcome to the chatroom! JS client connecting..."}
    (server/as-channel ring-req
      (let [username (-> ring-req :params :username)
            uid (uuid)] ; client uid
        {:on-receive (fn [ch message]
                       (println message)
                       (on-chat-msg! username (get (cheshire/parse-string message) "input-ws"))
                       (notify-clients (-> (chat/chatbox "test-element-ws") (hiccup/html) (str))))
         :on-close   (fn [ch status] 
                       (swap! clients/clients dissoc uid)
                       (swap! ava/author->avatar dissoc username)
                       (println (str "client " uid " disconnected with status " status)))
         :on-open    (fn [ch]
                       (swap! clients/clients assoc uid ch)
                       #_(server/send! ch uid)
                       (println @clients/clients))}))))

(defn ping-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "pong"})

(defn handler-receive [request]
  (notify-clients (json/write-str (utils/parse-params-str request)))
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "clients notified!"})

(defn test-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (-> (chat/chatroom-view (-> request :params :login)) (hiccup/html) (str))})

(defn login-request-handler
  [request]
  ;(println (:query-string request))
  (println request)
  {:status 200
   :headers {"Content-Type" "text/html", "HX-Redirect" "/test"}
   :body {:login (-> request :params :login)}
   })

(defn root-handler
  [request]
  ;(println (:query-string request))
  (println request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (-> (login/login-view) (hiccup/html) (str))})

(defroutes test-routes
  ;; html
  (GET "/" request (root-handler request))
  (GET "/login-request" request (login-request-handler request))
  (POST "/test" request (test-handler request))
  ;(GET "/button-test" request (test-button-press-handler request))
  ;; rest api
  (GET "/ping" request (ping-handler request))
  (GET "/receive" request (handler-receive request))
  (GET "/ws/connect" request (ws-connect-handler request)))

;; server 
(defonce server (atom nil))

(def app
  (-> test-routes
      (rmr/wrap-resource "public")
      rmkp/wrap-keyword-params
      rmp/wrap-params))

(defn start-server [port]
  (println "Starting server")
  (reset! server (server/run-server #'app {:port port :join? false})))

(defn stop-server []
  (when-not (nil? @server)
    (println "Stopping server")
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(comment)
