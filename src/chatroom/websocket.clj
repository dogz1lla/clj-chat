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
            [clojure.string :as s]
            [clojure.set :as cs]))


(defn notify-clients [msg & selected-ids]
  (let [all-channels @clients/clients
        send-to (if 
                  (seq selected-ids) 
                  (into {} (filter (fn [kv] ((first selected-ids) (first kv))) all-channels))
                  all-channels)]
    (doseq [[_ channel] send-to]
      (server/send! channel msg))))


; (defn notify-clients [msg]
;   (doseq [[_ channel] @clients/clients]
;     (server/send! channel msg)))

(defn on-chat-msg!
  " Add a new message to history and update the msg color state."
  [author msg]
  (when (not (get @ava/author->avatar author))
    (swap! ava/author->avatar assoc author (ava/random-avatar)))
  (swap! db/msg-log conj {:author author :body msg}))

;; see https://github.com/http-kit/http-kit/blob/master/src/org/httpkit/server.clj
(defn ws-connect-handler
  "Handle a websocket connection request.
  :on-receive
    - go and fetch which chat the message has been sent to by looking up which
      chat is active for the given user;
    - parse the json payload that is the message body;
    - look up the ids of the websocket connections; if the recipient is
      currently looking at the chat in question then add his connections' ids
      to the set;
    - construct the html str response;
    - add the new chat message to the db;
    - send the html over ws to relevant clients."
  [ring-req]
  (if-not (:websocket? ring-req)
    {:status 200 :body "Welcome to the chatroom! JS client connecting..."}
    (server/as-channel ring-req
      (let [username (-> ring-req :params :username)
            uid (utils/uuid)] ; websocket connection uid
        {:on-receive (fn [ch message]
                       (println message)
                       (let [other-user (db/get-users-active-chat username)
                             parsed-msg (get (cheshire/parse-string message) "input-ws")
                             username-conn-ids (clients/get-users-conns username)
                             recipient-conn-ids 
                             (if
                               (= (db/get-users-active-chat other-user) username)
                               (cs/union username-conn-ids (clients/get-users-conns other-user))
                               username-conn-ids)
                             chat-key (db/get-chat-key username other-user)]
                         (db/add-chat-msg! username other-user parsed-msg)
                         (notify-clients
                           (-> (chat/chatbox chat-key "test-element-ws")
                               (hiccup/html)
                               (str))
                           recipient-conn-ids))
                       #_(on-chat-msg! username (get (cheshire/parse-string message) "input-ws"))
                       #_(notify-clients (-> (chat/chatbox "test-element-ws") (hiccup/html) (str))))
         :on-close   (fn [ch status] 
                       (swap! clients/clients dissoc uid)
                       #_(swap! ava/author->avatar dissoc username)
                       (clients/rm-ws-conn! username uid)
                       (println (str "client " uid " disconnected with status " status)))
         :on-open    (fn [ch]
                       (swap! clients/clients assoc uid ch)
                       (db/add-user! username)
                       (db/init-chats-for-user! username)
                       (db/init-users-active-chat! username)
                       (clients/add-ws-conn! username uid)
                       (ava/init-user-avatar! username)
                       #_(println @clients/clients))}))))

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
   :body (-> (chat/chatroom-view (-> request :params :login) "announcements") (hiccup/html) (str))})

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

(defn switch-chat-handler
  [request]
  (let [username (-> request :params :username)
        other-user (-> request :params :otherUser)]
    (db/set-users-active-chat! username other-user)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (-> (chat/chatbox (db/get-chat-key username other-user) "test-element-ws") (hiccup/html) (str))}))

(defroutes test-routes
  ;; html
  (GET "/" request (root-handler request))
  (GET "/login-request" request (login-request-handler request))
  (POST "/test" request (test-handler request))
  (GET "/switch_chat" request (switch-chat-handler request))
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
