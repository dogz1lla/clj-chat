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
  "h-10 border-2 border-teal-500 pl-2")

(defn login-form-input-field-params []
  {:type "text"
   :name "login"
   :id "login-input-field"
   :placeholder "please enter your name"
   :class (login-form-input-field-style)})

(defn login-form-submit-style []
 "h-10 border-2 border-teal-500 pl-2 ml-2")

(defn login-form-submit-params []
  {:type "submit"
   :class (login-form-submit-style)})

(defn login-form []
  [:form (login-form-params)
    [:input (login-form-input-field-params)]
    [:input (login-form-submit-params)]])

(defn login-view-style []
 "m-auto w-1/2")

(defn login-view
  []
  [:body
   (htmx-init)
   (htmx-ws-init)
   (page/include-css "/css/output.css")
   [:div {:class (login-view-style)}
    (login-form)]])
