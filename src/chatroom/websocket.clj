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

(defn notify-users-clients
  "Send a message to all of the ws connections of a particular user."
  [username msg]
  (let [user-conns (clients/get-users-conns username)]
    (notify-clients msg user-conns)))

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
                             announcement-conn-ids
                             (if (= other-user "announcements")
                               (let [announcement-users (db/get-users-in-annoucements-chat)]
                                 (reduce
                                   (fn [c u] (into c (clients/get-users-conns u))) 
                                   #{}
                                   announcement-users)) ;notify ALL users who are looking at announcements
                               #{})
                             recipient-conn-ids  ; conn ids of the other user
                             (if
                               (= (db/get-users-active-chat other-user) username)
                               (cs/union username-conn-ids (clients/get-users-conns other-user))
                               username-conn-ids)
                             chat-key (db/get-chat-key username other-user)
                             recipient-conn-ids (cs/union recipient-conn-ids announcement-conn-ids)]
                         (db/add-chat-msg! username other-user parsed-msg)
                         (notify-clients
                           (-> (chat/chatbox chat-key "test-element-ws")
                               (hiccup/html)
                               (str))
                           recipient-conn-ids))
                       (notify-users-clients
                         username
                         (-> (chat/input-form-element)
                             (hiccup/html)
                             (str)))
                       )
         :on-close   (fn [ch status] 
                       (swap! clients/clients dissoc uid)
                       #_(swap! ava/author->avatar dissoc username)
                       (clients/rm-ws-conn! username uid)
                       (println (str "client " uid " disconnected with status " status)))
         :on-open    (fn [ch]
                       (let [new-user? (not ((db/get-users) username))]
                         (swap! clients/clients assoc uid ch)
                         (db/init-chats-for-user! username)
                         (db/add-user! username)
                         (db/init-users-active-chat! username)
                         (clients/add-ws-conn! username uid)
                         (ava/init-user-avatar! username)
                         ; if the username is connecting for the first time ever
                         ; then update the side buttons of all the other users
                         (when new-user?
                           (doseq [username (db/get-users)] 
                             (notify-users-clients
                               username
                               (-> (chat/generate-chat-buttons username)
                                   (hiccup/html)
                                   (str))))))
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
  (let [username (-> request :params :login)]
    (if (and (seq username) (< 1 (count username)))
      ; username is valid (present and long enough (>=2)) -> go to chat view
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (-> (chat/chatroom-view username "announcements")
                 (hiccup/html)
                 (str))}
      ; username is invalid -> redirect back to the login page with the error
      {:status 200
       :headers {"Content-Type" "text/html" "HX-Redirect" "/"}
       :body (-> (login/login-view "please provide a valid username!")
                 (hiccup/html)
                 (str))})))

(defn login-request-handler
  [request]
  ;(println (:query-string request))
  (println request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (-> (login/login-view) (hiccup/html) (str))})

(defn root-handler
  [request]
  ;(println (:query-string request))
  #_(println request)
  {:status 200
   :headers {"Content-Typej" "text/html"}
   :body "<h1>Please visit '/login' page</h1>"})

(defn switch-chat-handler
  [request]
  (let [username (-> request :params :username)
        other-user (-> request :params :otherUser)]
    (db/set-users-active-chat! username other-user)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (-> (chat/chatbox (db/get-chat-key username other-user) "test-element-ws")
               (hiccup/html)
               (str))}))

(defroutes test-routes
  ;; html
  (GET "/" request (root-handler request))
  (GET "/login" request (login-request-handler request))
  (POST "/chat" request (test-handler request))
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
