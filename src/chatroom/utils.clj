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
  (loop [result [] switch true i 0]
    (if (= i n) result (recur (conj result switch) (not switch) (inc i)))))
