;; how would the different rooms view work?
;; - what is the list of rooms anyway?
;; - should probably not call it rooms anymore but rather "chats";
;; - one chat per pair of users;
;; - the first time a user (defined by the username) joins the app they have
;;   two default chats: announcements and their own chat (for saving stuff)
;; - there should be a button to add a new chat; the choice consists of other
;;   users that are currently using the app;
;; - when some other user chooses you for a chat and sends you a message you
;;   should see a notification light (and the chat button gets created
;;   automatically if it doesnt exist yet for you) ;
(ns chatroom.components.chat
  (:require [hiccup.page :as page]
            [clojure.string :as s]
            [chatroom.components.avatar :as ava]
            [chatroom.utils :as utils]))


;; layout constants
(def width 500)

;; msg log cache
(def msg-log 
  (atom [{:author "announcements" :body "Greetings, welcome to dogz1lla's private chatroom!"}
         {:author "chat-bot" :body "hi"}
         {:author "chat-bot" :body "bye"}]))

(defn htmx-init []
  [:script {:src "https://unpkg.com/htmx.org@1.9.6"}])

(defn htmx-ws-init []
  [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}])

;; ----------------------------------------------------------------------------
;; chat and messages
(defn chat-msg-style-tailwind
  [dark?]
  (s/join 
    " "
    ["h-auto" (if dark? "bg-teal-500/75" "bg-teal-600/75") "m-1" "rounded-lg"]))

(defn chat-msg
  "see https://www.w3schools.com/howto/howto_css_chat.asp"
  [{:keys [author body]} dark?]
  [:div {:class (chat-msg-style-tailwind dark?)}
   [:span {:class "float-left relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full"}
     [:img {:src (get @ava/author->avatar author) :class "aspect-square h-full w-full"}]]
   [:p {:class "pl-12 italic text-yellow-400"} author]
   [:h8 {:style {:padding-left "5px"}} body]])

(defn chatbox [& id]
  [:div {:id (or id "chatbox")}
   (let [msgs @msg-log
         n (count msgs)
         seq-dark? (utils/alternating-true n)]
   (for [[msg dark?] (map vector msgs seq-dark?)] (chat-msg msg dark?)))])

;; ----------------------------------------------------------------------------
;; msg input element
(defn input-box-input-style []
  {:width "full"
   :height "40px"
   :border "1px solid black"})

(defn input-box-input-params []
  {:type "text"
   :name "input-ws"
   :id "input-field-ws"
   :class "w-full h-10 border-2 border-teal-500 pl-2"
   :placeholder "enter your message"})

(defn input-box-form-params []
  {:id "test-form-ws" :ws-send true})

(defn input-box-ws
  "Message input field"
  [username]
  [:div {:hx-ext "ws" :ws-connect (str "/ws/connect?username=" username)}
    [:form (input-box-form-params)
      [:input (input-box-input-params)]]])

;; ----------------------------------------------------------------------------
;; chat side panel button
(defn goto-chat-button
  []
  [:div {:class "w-5/6 h-auto m-1 aspect-square"}
   [:button {:type "button" :class "w-full h-full bg-gray-400 rounded-lg border-2 border-teal-500"} "D"]])

;; ----------------------------------------------------------------------------
;; chat element
(defn chat-element
  [username]
  [:div {:class "h-auto grid grid-cols-1 gap-4 lg:grid-cols-10 lg:gap-4"}
   [:div {:class "h-auto rounded-lg bg-gray-200"}
    (goto-chat-button)]
   [:div {:class "h-auto rounded-lg bg-gray-200 lg:col-span-9"}
    (chatbox "test-element-ws")
    (input-box-ws username)]])

;; ----------------------------------------------------------------------------
;; chat view
(defn chatroom-view
  "TODO figure out how to take out the init part outside"
  [username]
  (println (str "Greetings, " username))
  [:body 
   [:div {:class "m-auto w-2/3"}
     (htmx-init)
     (htmx-ws-init)
     (page/include-css "/css/output.css")
     (chat-element username)]
   ])

(comment
  (reset! msg-log [{:author "announcements" :body "Greetings, welcome to dogz1lla's private chatroom!"}])
  (map vector [1 2] [3 4])
  (map vector [{:1 2 :2 4}] [3 4])
  (chatbox)
  )
