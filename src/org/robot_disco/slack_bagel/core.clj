(ns org.robot-disco.slack-bagel.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clj-slack.channels :as schan]
            [clj-slack.mpim :as smpim]
            [clj-slack.chat :as schat]
            [clj-slack.rtm :as srtm]
            [clojure.string :as string]))

(def token (env :bagel-slack-token))
(def channel-name (env :bagel-channel-name))

(def connection {:api-url "https://slack.com/api"
                 :token token})

(def my-user-id
  (-> connection
      srtm/connect
      :self
      :id))

(defn find-channel [name]
  (->> (schan/list connection)
       :channels
       (filter #(= (:name %) name))
       first))

(defn pair-users [name]
  (let [channel (find-channel name)
        members (:members channel)]
    (partition 2 (shuffle members))))

(defn message-pairs [pairs]
  (doseq [pair pairs]
    (when (= (count pair) 2)
      (let [mpim (smpim/open connection (conj pair my-user-id))
            mpim-id (get-in mpim [:group :id])]
        (schat/post-message connection mpim-id "Hello! This week you have been matched up as conversation partners! I hope you meet up and have a great time :)")))))

(defn -main [& args]
  (let [pairs (pair-users channel-name)]
    (message-pairs pairs)))
