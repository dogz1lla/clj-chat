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


;; ----------------------------------------------------------------------------
;; styling units
(def uh 12)  ; unit height, 1 unit = 0.25 rem

;; ----------------------------------------------------------------------------
;; msg log cache
(defn htmx-init []
  [:script {:src "https://unpkg.com/htmx.org@1.9.6"}])

(defn htmx-ws-init []
  [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}])

;; ----------------------------------------------------------------------------
;; chat and messages
(defn chat-msg-style
  [dark?]
  (format "h-%s rounded-lg %s" uh (if dark? "bg-teal-500/75" "bg-teal-600/75")))

(defn user-avatar-element
  [src]
  [:span {:class 
          (s/join 
            " "
            ["float-left relative flex"
             (format "h-%s w-10" uh)
             "shrink-0 overflow-hidden rounded-full"])}
     [:img {:src src :class "aspect-square h-full w-full"}]])

(defn chat-msg
  "see https://www.w3schools.com/howto/howto_css_chat.asp"
  [{:keys [author body]} dark?]
  [:div {:class (chat-msg-style dark?)}
   (user-avatar-element (get @ava/author->avatar author))
   [:p {:class "pl-12 italic font-semibold text-yellow-400"} author]
   [:h8 {:class "pl-2"} body]])

(defn chatbox [chat-key & id]
  [:div {:id (or id "chatbox") :class (format "h-%s overflow-auto" (* 5 uh))}
   (let [msgs (get @db/chats chat-key)
         n (count msgs)
         seq-dark? (utils/alternating-true n)]
     (for [[msg dark?] (map vector msgs seq-dark?)] (chat-msg msg dark?)))])

;; ----------------------------------------------------------------------------
;; msg input element
; (defn input-box-input-style []
;   (format "w-full h-%s border-teal-500 pl-2" uh))
(defn input-box-input-style []
   "w-full h-full border-teal-500 pl-2")

(defn input-box-input-params []
  {:type "text"
   :name "input-ws"
   :id "input-field"
   :value ""
   :class (input-box-input-style)
   :autofocus true
   :placeholder "enter your message"})

(defn input-box-form-params []
  {:id "msg-input-form"
   :ws-send true})

(defn input-form-element
  []
  [:form (input-box-form-params)
    [:input (input-box-input-params)]])

(defn input-box-ws
  "Message input field"
  [username]
  [:div {:hx-ext "ws" :ws-connect (str "/ws/connect?username=" username)}
    (input-form-element)])

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

(defn goto-chat-button
  [username other-user]
  [:div {:class (goto-chat-style)}
   [:button (goto-chat-button-params username other-user) (subs (s/upper-case other-user) 0 2)]])

;; ----------------------------------------------------------------------------
;; chat element
(defn chat-element-style []
  (format
    "h-%s grid grid-cols-1 gap-4 lg:grid-cols-10 lg:gap-4"
    (* 6 uh)))

(defn chat-element-left-column-style []
  (format
    "h-%s rounded-lg bg-gray-200"
    (* 6 uh)))

(defn chat-element-right-column-style []
  (format
    "h-%s rounded-lg bg-gray-200 lg:col-span-9"
    (* 6 uh)))

(defn generate-chat-buttons
  [username]
  [:div {:id "side-buttons"}
    (let [all-chats (db/list-chats-for-user username)]
      (for [other-user all-chats] (goto-chat-button username other-user)))])

(defn chat-element
  [username other-user]
  [:div {:class (chat-element-style)}
   [:div {:class (chat-element-left-column-style)}
    (generate-chat-buttons username)]
   [:div {:class (chat-element-right-column-style)}
    [:div {:class (format "w-full h-%s grid grid-rows-6 gap-1" (* 6 uh))}
     [:div {:class (format "h-%s row-start-1 row-span-5 " (* 5 uh))}
        (chatbox (db/get-chat-key username other-user) "test-element-ws")]
     [:div {:class (format "h-%s row-start-6 row-span-1" uh)}
      (input-box-ws username)]]]])

;; ----------------------------------------------------------------------------
;; chat view
(defn chatroom-view-style []
  (format "w-2/3 h-%s m-auto" (* 6 uh)))

(defn chatroom-view
  "TODO figure out how to take out the init part outside"
  [username other-user]
  [:body {:class (format "h-%s" (* 6 uh))}
     (htmx-init)
     (htmx-ws-init)
     (page/include-css "/css/output.css")
     [:div {:class (chatroom-view-style)}
       (chat-element username other-user)]])

(comment
  (map vector [1 2] [3 4])
  (map vector [{:1 2 :2 4}] [3 4])
  (chatbox)
  (map #(= 0 (mod % 2)) (range 10))
  (input-box-input-style)
  (* 5 uh)
  )
