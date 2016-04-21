(ns ciste.logger
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]]))

(defn set-logger
  []
  (timbre/set-config!
   {:level :debug
    :ns-whitelist []
    :ns-blacklist []
    :middleware []
    :timestamp-opts timbre/default-timestamp-opts
    :appenders {:println (println-appender {:stream :auto})}}))
