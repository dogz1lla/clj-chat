(ns chatroom.utils)


(defn random-choice
  "Analogue of python's random.random_choice."
  [col]
  (first (shuffle col)))

(defn alternating-true
  "Col of alternating true and falses of given length."
  [n]
  (map #(= 1 (mod % 2)) (range n)))

(defn uuid
  "Random uuid as a string."
  []
  (str (java.util.UUID/randomUUID)))

(defn now
  "Java datetime object."
  []
  (new java.util.Date))


(comment
  (str (now)))
