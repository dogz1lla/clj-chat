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
          :body "Greetings, welcome to dogz1lla's chat app!"}
         {:author "chat-bot"
          :body "hi"}
         {:author "chat-bot"
          :body "bye"}]))

;; user set
(def users (atom #{:announcements}))
(defn add-user!
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
     :body "Greetings, welcome to dogz1lla's chat app!"}
    {:author "chat-bot"
     :body "hi"}
    {:author "chat-bot"
     :body "bye"}])

(def chats (atom {#{:announcements} announcements-log}))

(defn get-chat-key
  [username other-user]
  (let [username (keyword username)
        other-user (keyword other-user)]
    (if (= other-user :announcements) #{:announcements} #{username other-user})))

(defn init-chats-for-user!
  [username]
  (let [username (keyword username)]
  (when-not (@users username)
    (let [existing-users @users
          new-entries (reduce
                        (fn [m u]
                          (if-not
                            (= u :announcements)
                            (assoc m #{username u} [])
                            m)) 
                        {}
                        existing-users)]
      (swap! chats merge new-entries)))))

(defn list-chats-for-user [username]
  (let [username (keyword username)
        all-chats (keys @chats)]
    (conj (filter #(% username) all-chats) #{:announcements})))

(defn add-chat-msg!
  "Add a new message to the chat between two users.
  Note that the msg is a string that needs to be brought to the
  {:author username, :body msg} format to be stored in the db."
  [username other-user msg]
  (let [chat-key (get-chat-key username other-user)
        msgs-so-far (get @chats chat-key)]
    (swap! chats assoc chat-key (conj msgs-so-far {:author username :body msg}))))

;; # User state
;; ----------------------------------------------------------------------------
;; user->active-chat map stores which chat a given user is currently looking at
;; it is a map from str to str
(def user->active-chat
  (atom {}))

(defn set-users-active-chat! [username other-user]
  (swap! user->active-chat assoc username other-user))

(defn init-users-active-chat! [username]
  (set-users-active-chat! username "announcements"))

(defn get-users-active-chat [username]
  (get @user->active-chat username))

(comment
  (reset! msg-log [{:author "announcements"
                    :body "Greetings, welcome to dogz1lla's chat app!"}])
  (get {#{1, 2} 1} #{2, 1})
  {#{:dogz1lla :batman} []}
  (@users :dogz1lla)
  (@users :announcements)
  (add-user! "dogz1lla")
  (init-chats-for-user! "batman")
  (init-chats-for-user! "batman")
  (list-chats-for-user "dogz1lla")
  (disj #{1 2} 1)
  (init-users-active-chat "dogz1lla")
  (conj #{:1 :2} :2)
  (assoc {:1 1} :1 2)
  (merge {:1 1} {:1 2})
  #{1 1}
  (defn test-notify-clients [msg & selected-ids]
    (let [all-channels {}
          send-to (if selected-ids (into {} (filter (fn [kv] (selected-ids (first kv))) all-channels)) all-channels)]
      (doseq [[_ channel] send-to]
        (println channel msg))))
  (test-notify-clients "hi" #{"uid"})
  )
