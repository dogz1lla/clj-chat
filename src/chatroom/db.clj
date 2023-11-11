;; This ns is supposed to contain all of the logic necessary to communicate
;; with the database that holds all the message logs, usernames, rooms, etc.
;;
;; For the sake of the example this is simplified to clojure atoms instead.
;; map: user-id -> set of room-ids
;; map: room-id -> room msg log
;;
;; - User connects -> add it to the user->rooms map with default value
;;   #{:announcements}
;; - for simplicity make so that whenever a new user joins all the necessary
;;   rooms get created automatically for all the other connected users;
;; - new user should also be added to the room sets of all the other users;
(ns chatroom.db
  (:require [chatroom.utils :as utils]))


;; TODO: remove after fixing rooms (this will be in announcements)
(def msg-log 
  (atom [{:author "announcements"
          :body "Greetings, welcome to dogz1lla's private chatroom!"}
         {:author "chat-bot"
          :body "hi"}
         {:author "chat-bot"
          :body "bye"}]))

;; user set
(def users (atom #{:announcements}))
(defn add-user
  "Upon connecting, a username is added to the set of users using the app."
  [username]
  (swap! users conj (keyword username)))

;; # Chats
;; ----------------------------------------------------------------------------
;; Chat logs between each pair of users.
;; This is a map from #{user1, user2} -> vector of messages.
;; #{:announcements} room is special; the key has only one user "announcements"
;; and it is shared with all the other users.
(def announcements-log
   [{:author "announcements"
     :body "Greetings, welcome to dogz1lla's private chatroom!"}
    {:author "chat-bot"
     :body "hi"}
    {:author "chat-bot"
     :body "bye"}])

(def chats (atom {#{:announcements} announcements-log}))

(defn init-chats-for-user [username]
  (let [username (keyword username)
        existing-users @users
        new-entries (reduce
                      (fn [m u]
                        (if-not
                          (= u :announcements)
                          (assoc m #{username u} [])
                          m)) 
                      {}
                      existing-users)]
    (swap! chats merge new-entries)))

(defn list-chats-for-user [username]
  (let [username (keyword username)
        all-chats (keys @chats)]
    (conj (filter #(% username) all-chats) #{:announcements})))

; (def user->rooms (atom {}))
; (def room->msgs 
;   (atom {:announcements [{:author "announcements"
;                           :body "Greetings, welcome to dogz1lla's chat app!"}
;                          {:author "chat-bot"
;                           :body "hi"}
;                          {:author "chat-bot"
;                           :body "bye"}]}))
;
; (defn rooms-default [username]
;   #{:announcements (keyword username)})
;
; (defn rooms-add-users [rooms]
;   (reduce conj rooms (keys @user->rooms)))

(comment
  (reset! msg-log [{:author "announcements"
                    :body "Greetings, welcome to dogz1lla's private chatroom!"}])
  {"dogz1lla" #{"announcements" "another-room"}}
  {"announcements" @msg-log "another-room" []}
  (conj #{"hi"} "bye")
  (rooms-add-users (rooms-default "dogz1lla"))
  (get {#{1, 2} 1} #{2, 1})
  {#{:dogz1lla :batman} []}
  (@users :dogz1lla)
  (@users :announcements)
  (add-user "dogz1lla")
  (init-chats-for-user "batman")
  (list-chats-for-user "dogz1lla")
  (disj #{1 2} 1)
  )
