(ns chatroom.components.login
  (:require [hiccup.page :as page]))

;; TODO: fiqure out how to load htmx only once
(defn htmx-init []
  [:script {:src "https://unpkg.com/htmx.org@1.9.6"}])

(defn htmx-ws-init []
  [:script {:src "https://unpkg.com/htmx.org/dist/ext/ws.js"}])

(defn login-form-params []
  {:id "login-form" :action "/test" :method "post"})

(defn login-form-input-field-style []
  "h-10 w-full border-2 border-[#bd93f9] pl-2 mt-4")

(defn login-form-input-field-params []
  {:type "text"
   :name "login"
   :id "login-input-field"
   :placeholder "please enter your nickname"
   :class (login-form-input-field-style)})

(defn login-form-submit-style []
 "h-10 w-full border-2 border-[#50fa7b] mt-4 rounded-lg")

(defn login-form-submit-params []
  {:type "submit"
   :class (login-form-submit-style)})

(defn login-form []
  [:form (login-form-params)
    [:input (login-form-input-field-params)]
    [:input (login-form-submit-params)]])

(defn login-view-style []
 "m-auto w-[20rem] h-[50rem] ")

(defn login-view
  [& errors]
  [:body
   (htmx-init)
   (htmx-ws-init)
   (page/include-css "/css/output.css")
   [:div {:class (login-view-style)}
    (login-form)
    (when (seq errors)
     [:div {:class "m-auto pl-2 text-[#ff5555]"}
      (for [error errors] [:li error])])]])
