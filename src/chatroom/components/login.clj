(ns chatroom.components.login
  (:require [hiccup.page :as page]))

;; TODO: fiqure out how to load htmx only once
(defn htmx-init []
  [:script {:src "https://unpkg.com/htmx.org@1.9.6"}])

(defn htmx-ws-init []
  [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}])

#_(defn login-form
  []
  [:form {:id "login-form"}
    [:input {:type "text" :name "login" :id "login-input-field" :style {:width "1000px" :height "40px" :border "1px solid black"} :placeholder "please enter your name"}]
    [:button {:type "button" :hx-include "#login-form" :hx-get "/login-request" :hx-trigger "click"} "connect"]
    ]
  )

(defn login-form
  []
  [:form {:id "login-form" :action "/test" :method "post"}
    [:input {:type "text" :name "login" :id "login-input-field" :placeholder "please enter your name" :class "h-10 border-2 border-teal-500 pl-2"}]
    [:input {:type "submit" :class "h-10 border-2 border-teal-500 pl-2 ml-2"}]
    ]
  )

(defn login-view
  []
  [:body
   (htmx-init)
   (htmx-ws-init)
   (page/include-css "/css/output.css")
   [:div {:class "m-auto w-1/2"}
    (login-form)]])
