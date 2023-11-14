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
;;
;; TODO: take out "test-element-ws" id of the chatbox into a var
(ns chatroom.components.chat
  (:require [hiccup.page :as page]
            [clojure.string :as s]
            [chatroom.components.avatar :as ava]
            [chatroom.utils :as utils]
            [chatroom.db :as db]))


;; msg log cache
(defn htmx-init []
  [:script {:src "https://unpkg.com/htmx.org@1.9.6"}])

(defn htmx-ws-init []
  [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}])

;; ----------------------------------------------------------------------------
;; chat and messages
(defn chat-msg-style
  [dark?]
  (s/join 
    " "
    ["h-auto m-1 rounded-lg" (if dark? "bg-teal-500/75" "bg-teal-600/75")]))

(defn chat-msg
  "see https://www.w3schools.com/howto/howto_css_chat.asp"
  [{:keys [author body]} dark?]
  [:div {:class (chat-msg-style dark?)}
   [:span {:class "float-left relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full"}
     [:img {:src (get @ava/author->avatar author) :class "aspect-square h-full w-full"}]]
   [:p {:class "pl-12 italic text-yellow-400"} author]
   [:h8 {:class "pl-2"} body]])

(defn chatbox [chat-key & id]
  [:div {:id (or id "chatbox")}
   (let [msgs (get @db/chats chat-key)
         n (count msgs)
         seq-dark? (utils/alternating-true n)]
     (for [[msg dark?] (map vector msgs seq-dark?)] (chat-msg msg dark?)))])

;; ----------------------------------------------------------------------------
;; msg input element
(defn input-box-input-style []
  "w-full h-10 border-2 border-teal-500 pl-2")

(defn input-box-input-params []
  {:type "text"
   :name "input-ws"
   :id "input-field-ws"
   :class (input-box-input-style)
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
(defn goto-chat-style []
  "w-5/6 h-auto m-1 aspect-square")

(defn goto-chat-button-style []
 "w-full h-full bg-gray-400 rounded-lg border-2 border-teal-500")

(defn goto-chat-button-params [username other-user]
  {:type "button"
   :hx-get (format "/switch_chat?username=%s&otherUser=%s" username (or other-user "announcements"))
   :hx-target "#test-element-ws"
   :hx-swap "outerHTML"
   :class (goto-chat-button-style)})

; (defn goto-chat-button
;   [username]
;   [:div {:class (goto-chat-style)}
;    [:button (goto-chat-button-params username) "D"]])

(defn goto-chat-button
  [username other-user]
  [:div {:class (goto-chat-style)}
   [:button (goto-chat-button-params username other-user) (subs (s/upper-case other-user) 0 2)]])

;; ----------------------------------------------------------------------------
;; chat element
(defn chat-element-style []
  "h-auto grid grid-cols-1 gap-4 lg:grid-cols-10 lg:gap-4")

(defn chat-element-left-column-style []
  "h-auto rounded-lg bg-gray-200")

(defn chat-element-right-column-style []
 "h-auto rounded-lg bg-gray-200 lg:col-span-9")

(defn chat-element
  [username other-user]
  [:div {:class (chat-element-style)}
   [:div {:class (chat-element-left-column-style)}
    (let [all-chats (db/list-chats-for-user username)]
      (for [other-user all-chats] (goto-chat-button username other-user)))
    #_(goto-chat-button username)
    ]
   [:div {:class (chat-element-right-column-style)}
    (chatbox (db/get-chat-key username other-user) "test-element-ws")
    (input-box-ws username)]])

;; ----------------------------------------------------------------------------
;; chat view
(defn chatroom-view-style []
  "m-auto w-2/3")

(defn chatroom-view
  "TODO figure out how to take out the init part outside"
  [username other-user]
  ;(println (str "Greetings, " username))
  [:body 
   [:div {:class (chatroom-view-style)}
     (htmx-init)
     (htmx-ws-init)
     (page/include-css "/css/output.css")
     (chat-element username other-user)]
   ])

(comment
  (map vector [1 2] [3 4])
  (map vector [{:1 2 :2 4}] [3 4])
  (chatbox)
  )
