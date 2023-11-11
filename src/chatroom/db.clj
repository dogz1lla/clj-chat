;; This ns is supposed to contain all of the logic necessary to communicate
;; with the database that holds all the message logs, usernames, rooms, etc.
;;
;; For the sake of the example this is simplified to clojure atoms instead.
;; map: user-id -> set of room-ids
;; map: room-id -> room msg log
(ns chatroom.db)


(def msg-log 
  (atom [{:author "announcements"
          :body "Greetings, welcome to dogz1lla's private chatroom!"}
         {:author "chat-bot"
          :body "hi"}
         {:author "chat-bot"
          :body "bye"}]))

(def user->rooms (atom {}))
(def room->msgs 
  (atom {:announcements [{:author "announcements"
                          :body "Greetings, welcome to dogz1lla's chat app!"}
                         {:author "chat-bot"
                          :body "hi"}
                         {:author "chat-bot"
                          :body "bye"}]}))

(comment
  (reset! msg-log [{:author "announcements"
                    :body "Greetings, welcome to dogz1lla's private chatroom!"}])
  {"dogz1lla" #{"announcements" "another-room"}}
  {"announcements" @msg-log "another-room" []}
  )
