(ns chatroom.clients
  "Logic to handle ws connections for users."
  (:require [clojure.set :as cs]))


(defonce clients (atom {}))
(def user->ws-conn-id (atom {}))

(defn add-ws-conn!
  "Add a ws connection id to the set of ids associated with username or init it
  to the set of one item."
  [username conn-uid]
  (let [username (keyword username)
        conns (@user->ws-conn-id username)]
    (if conns
      (swap! user->ws-conn-id assoc username (conj conns conn-uid))
      (swap! user->ws-conn-id assoc username #{conn-uid}))))

(defn rm-ws-conn!
  "Dissoc the ws connection id from the set of ids of the username."
  [username conn-uid]
  (let [username (keyword username)
        conns (@user->ws-conn-id username)]
    (swap! user->ws-conn-id assoc username (disj conns conn-uid))))

(defn get-users-conns
  "Return a set of conn ids for a given user."
  [username]
  (get @user->ws-conn-id (keyword username)))

(comment
  (:ei {:hi "bye"})
  ({:hi "bye"} :ei)
  (add-ws-conn! "dogz1lla" "uid")
  (add-ws-conn! "dogz1lla" "uid2")
  (rm-ws-conn! "dogz1lla" "uid")
  (into {} (filter (fn [kv] (#{:1} (first kv))) {:1 1 :2 2}))
  (cs/union #{1 2} #{3 4 2})
  )
