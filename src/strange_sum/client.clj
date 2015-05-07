(ns strange-sum.client
  (:require [clj-http.client :as http]
            [clojure.xml :as xml]
            [clojure.data.json :as json]))

(def api-key "steam api key goes here")

(defn- get-community-url
  [steam-id]
  (format "http://steamcommunity.com/id/%s/?xml=1" steam-id))

(defn- request-steam-id
  [steam-id]
  (first (:content
          (first (:content (xml/parse (get-community-url steam-id)))))))

(defn- get-backpack-url
  [id]
  (format (str "http://api.steampowered.com/IEconItems_440/GetPlayerItems/v0001"
               "/?key=%s&SteamID=%s") api-key id))

(def get-steam-id (memoize request-steam-id))

(defn get-backpack
  [id]
  (json/read-str (:body (http/get (get-backpack-url id))) :key-fn keyword))

(defn- strange?
  [item]
  (= 11 (:quality item)))

(defn get-strange-items
  [id]
  (filter strange? (get-in (get-backpack id) [:result :items] [])))

(defn- kill-attribute?
  [attr]
  (= 214 (:defindex attr)))

(defn strange-sum
  [id]
  (->> (get-steam-id id)
       (get-strange-items)
       (map :attributes)
       (flatten)
       (filter kill-attribute?)
       (map :value)
       (reduce +)))
