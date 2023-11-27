;; TODO: take out "test-element-ws" id of the chatbox into a var
(ns chatroom.components.chat
  "Chat views."
  (:require [hiccup.page :as page]
            [clojure.string :as s]
            [chatroom.components.avatar :as ava]
            [chatroom.utils :as utils]
            [chatroom.db :as db]))


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
  (s/join 
    " "
    ["h-auto"
     "w-[53rem] mx-[0.75rem] my-[0.1rem]"
     "rounded-lg"
     (if dark? "bg-[#4f475a]" "bg-[#44475a] ")]))

(defn user-avatar-element
  [src]
  [:span {:class 
          (s/join 
            " "
            ["float-left relative flex"
             "h-10 w-10" 
             "shrink-0 overflow-hidden rounded-full"])}
     [:img {:src src :class "aspect-square h-full w-full"}]])

(defn chat-msg
  "see https://www.w3schools.com/howto/howto_css_chat.asp"
  [{:keys [author body timestamp]} dark?]
  [:div {:class (chat-msg-style dark?)}
   (user-avatar-element (get @ava/author->avatar author))
   [:p {:class "pl-12 italic font-semibold text-[#ff5555]"} author]
   [:h8 {:class "pl-2 pl-2 text-[#f8f8f2]"} body]
   [:span {:style {:float "right" :padding-right "0.5rem" :font-size "0.75rem" :color "#ffb86c"}}
    timestamp]])

(defn chatbox [chat-key & id]
  [:div {:id (or id "chatbox") :class "h-full w-full overflow-auto flex flex-col-reverse"}
   (let [msgs (reverse (get @db/chats chat-key))
         n (count msgs)
         seq-dark? (utils/alternating-true n)]
     (for [[msg dark?] (map vector msgs seq-dark?)] (chat-msg msg dark?)))])

;; ----------------------------------------------------------------------------
;; msg input element
(defn input-box-input-style []
   "w-full h-full border border-[#282a36] pl-2")

(defn input-box-input-params []
  {:type "text"
   :name "input-ws"
   :id "input-field"
   :value ""
   :class (input-box-input-style)
   :autofocus true
   :placeholder "enter your message here and then hit <Enter> to send it!"})

(defn input-box-form-params []
  {:id "msg-input-form"
   :ws-send true
   :class "h-[3rem] "})

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
  "w-[5rem] h-[5rem] mx-auto my-1 aspect-square")

(defn goto-chat-button-style []
 "w-full h-full bg-[#44475a] rounded-lg border-2 border-[#bd93f9] text-[#f1fa8c]")

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
  "h-[38rem] w-[60rem] grid grid-cols-1 gap-1 lg:grid-cols-11 lg:gap-2")

(defn chat-element-left-column-style []
  "h-[38rem] w-[5.5rem] bg-[#282a36] border border-1 rounded-lg overflow-auto")

(defn chat-element-right-column-style []
  "h-[38rem] w-[54.5rem] bg-[#282a36] lg:col-span-10")

(defn generate-chat-buttons
  [username]
  [:div {:id "side-buttons" :class "mx-auto"}
    (let [all-chats (db/list-chats-for-user username)]
      (for [other-user all-chats] (goto-chat-button username other-user)))])

(defn chat-element
  [username other-user]
  [:div {:class (chat-element-style)}
   [:div {:class (chat-element-left-column-style)}
    (generate-chat-buttons username)]
   [:div {:class (chat-element-right-column-style)}
    [:div {:class "h-[38rem] w-full grid grid-rows-11 gap-2"}
     [:div {:class "h-[34.5rem] w-full row-start-1 row-span-10"}
        (chatbox (db/get-chat-key username other-user) "test-element-ws")]
     [:div {:class "h-[3rem] w-full row-start-11 row-span-1"}
      (input-box-ws username)]]]])

;; ----------------------------------------------------------------------------
;; chat view
;; n*w_column + (n-1)*w_gap
;; n = 11; w_column = 5rem, w_gap = 0.5rem -> width = 55+5 = 60
;; for rows:
;; n = 11, h_row = 3rem, h_gap = 0.5 -> height = 33+5 = 38
(defn chatroom-view-style []
  "h-[38rem] w-[60rem] m-auto")

(defn chatroom-view
  "Main chat app view. Requires the username of the user and also the username
  of the second user (recipient of the messages)."
  [username other-user]
  [:body {:class "h-[40rem] w-[61rem] m-auto"}
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
  (if (seq "hi") true false)
  (< 1 (count "hi"))
  )
