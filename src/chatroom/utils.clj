(ns chatroom.utils
  (:require [clojure.string :as s]))


(defn parse-params-str [request]
  (let [params-str (:query-string request)
        params-lst (s/split params-str #"&")]
    (into {} (map #(s/split % #"=") params-lst))))

(defn random-choice
  [col]
  (first (shuffle col)))

(defn alternating-true
  [n]
  (map #(= 1 (mod % 2)) (range n)))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn now [] (new java.util.Date))


(comment
  (str (now)))
