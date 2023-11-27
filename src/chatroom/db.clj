(ns chatroom.db
  "This ns is supposed to contain all of the logic necessary to communicate 
  with the database that holds all the message logs, usernames, rooms, etc."
  (:require [chatroom.utils :as utils]))


;; ----------------------------------------------------------------------------
;; # Users
(def users (atom #{"announcements"}))

(defn add-user!
  "Upon connecting, a username is added to the set of users using the app."
  [username]
  (swap! users conj username))

(defn get-users
  []
  (disj @users "announcements"))

;; ----------------------------------------------------------------------------
;; # Chats
;; Chat logs between each pair of users.
;; This is a map from #{user1, user2} -> vector of messages.
;; #{:announcements} room is special; the key has only one user "announcements"
;; and it is shared with all the other users.
(def announcements-log
   [{:author "announcements"
     :body "Greetings, welcome to dogz1lla's chat app!"
     :timestamp "Sun Nov 26 00:00:00 CET 2023"}
    {:author "announcements"
     :body "This is an 'announcements' chat. It is a default chat for any new user."
     :timestamp "Sun Nov 26 00:00:01 CET 2023"}
    {:author "chat-bot"
     :body "hi"
     :timestamp "Sun Nov 26 00:00:02 CET 2023"}
    {:author "chat-bot"
     :body "bye"
     :timestamp "Sun Nov 26 00:00:03 CET 2023"}])

(def chats (atom {#{"announcements"} announcements-log}))

(defn get-chat-key
  [username other-user]
  (if (= other-user "announcements") #{"announcements"} #{username other-user}))

(defn init-chats-for-user!
  "If the user is not among the existing users then initialize all chats
  between this user and others to empty vector."
  [username]
  (when-not (@users username)
    (let [existing-users @users
          new-entries (reduce
                        (fn [m u]
                          (if-not
                            (= u "announcements")
                            (assoc m #{username u} [])
                            m)) 
                        {}
                        existing-users)]
      (swap! chats merge new-entries))))

(defn list-chats-for-user
  "List of chats for user == all users except the given user."
  [username]
  (let [all-users @users]
    (disj all-users username)))

(defn add-chat-msg!
  "Add a new message to the chat between two users.
  Note that the msg is a string that needs to be brought to the
  {:author username, :body msg :timestamp timestamp} format to be stored in the
  db."
  [username other-user msg]
  (let [chat-key (get-chat-key username other-user)
        msgs-so-far (get @chats chat-key)]
    (swap! chats assoc chat-key 
           (conj msgs-so-far {:author username :body msg :timestamp (str (utils/now))}))))

;; ----------------------------------------------------------------------------
;; # User state
;; user->active-chat map stores which chat a given user is currently looking at
;; it is a map from str to str
(def user->active-chat
  (atom {}))

(defn set-users-active-chat!
  "Record which chatroom view the given user is currently looking at."
  [username other-user]
  (swap! user->active-chat assoc username other-user))

(defn init-users-active-chat! [username]
  (set-users-active-chat! username "announcements"))

(defn get-users-active-chat [username]
  (get @user->active-chat username))

(defn get-users-in-annoucements-chat
  "Return a set of users that are currently looking at the announcements chat."
  []
  (let [all-users @users
        active-chats @user->active-chat]
    (->> all-users
        (filter (fn [u] (= "announcements" (get active-chats u)))))))

(comment
  (reset! msg-log [{:author "announcements"
                    :body "Greetings, welcome to dogz1lla's chat app!"}])
  (get {#{1, 2} 1} #{2, 1})
  {#{:dogz1lla :batman} []}
  (@users :dogz1lla)
  (@users "announcements")
  (add-user! "dogz1lla")
  (init-chats-for-user! "batman")
  (init-chats-for-user! "batman")
  (list-chats-for-user "dogz1lla")
  (disj #{1 2} 1)
  (init-users-active-chat "dogz1lla")
  (conj #{:1 :2} :2)
  (assoc {:1 1} :1 2)
  (merge {:1 1} {:1 2})
  (test-notify-clients "hi" #{"uid"})
  (name :hi)
  (conj [1] 2)
  (into #{1} #{2 3})
  (#{1 2} 2)
  (merge {#{"announcements"} []} (reduce
    (fn [m u]
      (if-not
        (= u "announcements")
        (assoc m #{:test u} [])
        m)) 
    {}
    #{:a :b "announcements"}))
  )
