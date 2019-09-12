(ns org.robot-disco.slack-bagel.core
  (:require [environ.core :refer [env]]
            [clj-slack.channels :as sc]
            [clj-slack.mpim :as sm]))

(def token (env :slack-token))
(def channel-name (env :bagel-channel-name))

(def connection {:api-url "https://slack.com/api"
                 :token token})

(defn find-channel [name]
  (->> (sc/list connection)
       :channels
       (filter #(= (:name %) name))
       first))

(defn pair-users [name]
  (let [channel (find-channel name)
        members (:members channel)]
    (partition 2 (shuffle members))))
