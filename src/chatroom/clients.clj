#_" websocket client manager module.
Need the following functionality:
- to connect to the websocket server without actually connection to any
  channels right away;
- create chatrooms with the ability for clients to join/leave; each client may
  be a member of zero or more chatrooms at the same time;
- once a client connects to a room the history of messages should be restored
  on their side;
- after the client connects to a websocket, await for a specific type of msg
  like 'connect chatroom-id' and then the server connects it to the specified
  chatroom (and creates one if it doesnt exist); this way the client can connect
  to more than one room over the same websocket;
  this calls for specifying how a message should look like;

So what is a chatroom exactly?
A chatroom is an object that keeps references to all client websocket channels;
When one client sends a message through a websocket it is broadcasted to all
clients;
---------
Design:
- web server running with a ws endpoint available;
- client accesses the ws endpoint and if  the connect is successful gets its id
  from the server back; now every further msg should be accompanied by the id;
- client can send commands;
- connect command has to contain the id of the room the client wants to connect
  to;
- post command has to contain the body of the msg and the room id;
- leave command has to contain room id;
- each client id is a str that is mapped to websocket connection obj;
- each room id is mapped onto a set of client ids;

NEXT: start with the htmx gui
---------
- there is a chat window that has the message history;
- once you load the page you can now see the history of messaged and send new
  ones;
- button press triggers the message sending; it should update the chat element
  to show everything so far plus the new message;
"
(ns chatroom.clients
  (:require [clojure.string :as s]))


(defonce clients (atom {}))
(defonce rooms (atom {}))

;; message parsing
(defn valid-msg?
  " Checks if the message is of the format k1=v1&k2=v2&..."
  [msg]
  (let [splits (s/split msg #"&")]
    (every? #(re-matches #".+=.+" %) splits)))

(defn parse-msg
  "Message should comply with the following format:
      client=clientid&command=somecommand&room=roomid&data=optionaldata"
  [raw-msg]
  (if (valid-msg? raw-msg)
   (let [splits (s/split raw-msg #"&")
         kvs (map #(s/split % #"=") splits)
         kvs (map (fn [[k v]] [(keyword k) v]) kvs)]
    (into {} kvs)) 
    raw-msg))

(defn valid-command? [msg]
  (let [valid-commands #{"connect" "leave" "post"}
        command (:command msg)]
    (valid-commands command)))

(defn connect-room? [msg]
  (= (:command msg) "connect"))

(defn leave-room? [msg]
  (= (:command msg) "leave"))

(defn post-to-room? [msg]
  (= (:command msg) "post"))

(defn process-msg
  "Things that should be possible
  - 'connect to room' command;
  - 'leave a room' command;
  - 'post a msg to a room' command;
  "
  [raw-msg]
  (let [msg (parse-msg (s/trim-newline raw-msg))]
    (if (and (:client msg) (valid-command? msg))
      msg
      {:error (str "message " msg " is invalid")})))

(defn connect-room! [room-id client-id]
  (let [room (get @rooms room-id)
        updated-room (if room (conj room client-id) #{client-id})]
    (swap! rooms assoc room-id updated-room)))

(defn leave-room! [room-id client-id]
  (let [room (get @rooms room-id)
        updated-room (disj room client-id)]
    (swap! rooms assoc room-id updated-room)))

(defn post-room! [room-id client-id msg-body]
  (let [room (get @rooms room-id)]
    (println (str client-id " says to the room " room ": " msg-body))))

(defn execute-command! [msg]
  #_(println (str "TEST: msg " msg))
  (case (:command msg)
    "connect" (connect-room! (:room msg) (:client msg))
    "leave" (leave-room! (:room msg) (:client msg))
    "post" (post-room! (:room msg) (:client msg) (:body msg))
    msg
    )
  #_(println @rooms))

;; --------
;; --------
(defonce channels (atom #{}))

(defn connect! [channel]
  (println "Channel open")
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (println (str "Channel closed, status: " status))
  (swap! channels #(remove #{channel} %)))

;; --- New code below
(comment
  (defn broadcast! [msg room]
    (doseq [client room] (server/send! client msg)))
  (defn get-msg [room-id client-id body]
    {:room-id room-id :author client-id :body body})
  (defn send-msg [room-id client-id body]
    (let [room (get rooms room-id)
          msg (get-msg room-id client-id body)]
      (broadcast! msg room)))
  (defn connect-room! [room-id client-channel]
    ; - lookup the room in the rooms map
    ; - add the client channel to the room's clients set
    )
  (def test-client {:id "test-client" :channel nil})
  (def clients-cache {"test-client" test-client})
  (def test-room #{"test-client"})
  (def rooms (atom #{test-room}))
  (valid-msg? "hi=bye")
  (every? #(re-matches #".+=.+" %) ["hi=bye" "byehi"])
  (parse-msg  "hi=bye&bye=hi")
  (execute-command! (process-msg "command=connect&client=1"))
  (#{"connect" "leave" "post"} "connect")
  (valid-command? {:command "connect"})
  (valid-msg? "command=connect&client=c0f39eb5-7c36-459a-af73-911bafbf9698&room=1")
  (parse-msg "command=connect&client=c0f39eb5-7c36-459a-af73-911bafbf9698&room=1")
  (valid-command? (parse-msg "command=connect&client=c0f39eb5-7c36-459a-af73-911bafbf9698&room=1"))
  )
