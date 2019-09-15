(ns net.robot-disco.slack-bagel.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clj-slack.channels :as schan]
            [clj-slack.mpim :as smpim]
            [clj-slack.chat :as schat]
            [clj-slack.rtm :as srtm]
            [clojure.string :as string]))

(def token
  "The OAuth Access Token registered under the app this software will run as.

  NOTE: This is the regular, non-bot `xoxp-`-prefixed token. The OAuth Bot Access Token does not work."
  (env :bagel-slack-token))

(def channel-name
  "The Slack Channel this app will use for matching people."
  (env :bagel-channel-name))

(def connection {:api-url "https://slack.com/api"
                 :token token})

(def my-user-id
  ;; I can't find a better way to determine the app bot user's Slack User ID then by pulling it from a RTM connection initiation.
  ;; In theory `bot.info` works but it never showed me any information when I tried it!
  (-> connection
      srtm/connect
      :self
      :id))

(defn find-channel [name]
  "Get the API object back for the Slack channel with the text identifier `name`"
  (->> (schan/list connection)
       :channels
       (filter #(= (:name %) name))
       first))

(defn pair-users [name]
  "Grab all users from slack channel named `name`, and randomly pair them.

  This should not causee any issues if there are an odd-number of people in a channel, but one person won't be matched."
  (let [channel (find-channel name)
        members (:members channel)]
    (partition 2 (shuffle members))))

(defn message-pairs [pairs]
  "Given a sequence `pairs` of 2-element seqs containing two Slack User ID Strings, create a DM between them and tell them they've been matched."
  (doseq [pair pairs]
    ;; We don't want to do this if we get a non two-tuple sequence ... which shouldn't be returned by `partition`, but just in case...
    (when (= (count pair) 2)
      (let [mpim (smpim/open connection (conj pair my-user-id))
            mpim-id (get-in mpim [:group :id])]
        (schat/post-message connection mpim-id "Hello! This week you have been matched up as conversation partners! I hope you meet up and have a great time :)")))))

(defn -main [& args]
  (let [pairs (pair-users channel-name)]
    (message-pairs pairs)))
