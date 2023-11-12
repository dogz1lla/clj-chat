(ns chatroom.components.avatar
  (:require [clojure.java.io :as io]
            [chatroom.utils :as utils]))


(def avatars-url "https://avataaars.io/?avatarStyle=Circle&")

(def facial-hair-options
  '("Blank"
    "BeardMedium"
    "BeardLight"
    "BeardMajestic"
    "MoustacheFancy"
    "MoustacheMagnum"))

(def head-top-options
  '("NoHair"
    "Eyepatch"
    "Hat"
    "Hijab"
    "Turban"
    "WinterHat1"
    "WinterHat2"
    "WinterHat3"
    "WinterHat4"
    "LongHairBigHair"
    "LongHairBob"
    "LongHairBun"
    "LongHairCurly"
    "LongHairCurvy"
    "LongHairDreads"
    "LongHairFrida"
    "LongHairFro"
    "LongHairFroBand"
    "LongHairNotTooLong"
    "LongHairShavedSides"
    "LongHairMiaWallace"
    "LongHairStraight"
    "LongHairStraight2"
    "LongHairStraightStrand"
    "ShortHairDreads01"
    "ShortHairDreads02"
    "ShortHairFrizzle"
    "ShortHairShaggyMullet"
    "ShortHairShortCurly"
    "ShortHairShortFlat"
    "ShortHairShortRound"
    "ShortHairShortWaved"
    "ShortHairSides"
    "ShortHairTheCaesar"
    "ShortHairTheCaesarSidePart"))

(def clothes-options
  '("BlazerShirt"
    "BlazerSweater"
    "CollarSweater"
    "GraphicShirt"
    "Hoodie"
    "Overall"
    "ShirtCrewNeck"
    "ShirtScoopNeck"
    "ShirtVNeck"))

(def accessories-options
  '("Blank"
    "Kurt"
    "Prescription01"
    "Prescription02"
    "Round"
    "Sunglasses"
    "Wayfarers"))

;; avatar cache
(defn avatar-img
  [facial-hair head-top clothes accessories]
  (str
    avatars-url
    (format
      "facialHairType=%s&topType=%s&clotheType=%s&accessoriesType=%s"
      facial-hair
      head-top
      clothes
      accessories)))

(def author->avatar
  (atom 
    {"announcements" (io/file "/imgs/announcement.jpeg")
     "chat-bot" (io/file "/imgs/robot.png")}))

;; ----------------------------------------------------------------------------
;; random avatar stuff
(defn random-avatar
  []
  (avatar-img
    (utils/random-choice facial-hair-options)
    (utils/random-choice head-top-options)
    (utils/random-choice clothes-options)
    (utils/random-choice accessories-options)))

(defn init-user-avatar! 
  [username]
  (let [existing-avatar (get @author->avatar username)]
    (when-not existing-avatar
      (swap! author->avatar assoc username (random-avatar)))))
