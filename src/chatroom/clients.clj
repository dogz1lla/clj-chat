(ns chatroom.clients)


(defonce clients (atom {}))
(def user->ws-conn-id (atom {}))

(defn add-ws-conn!
  [username conn-uid]
  (let [username (keyword username)
        conns (@user->ws-conn-id username)]
    (if conns
      (swap! user->ws-conn-id assoc username (conj conns conn-uid))
      (swap! user->ws-conn-id assoc username #{conn-uid}))))

(defn rm-ws-conn!
  [username conn-uid]
  (let [username (keyword username)
        conns (@user->ws-conn-id username)]
    (swap! user->ws-conn-id assoc username (disj conns conn-uid))))

(comment
  (:ei {:hi "bye"})
  ({:hi "bye"} :ei)
  (add-ws-conn! "dogz1lla" "uid")
  (rm-ws-conn! "dogz1lla" "uid")
  )
