(ns ciste.config)

(defonce ^:dynamic *environment* :development)

(defonce ^:dynamic *environments* (ref {}))

(defn config
  []
  (get @*environments* *environment*))

(defn environment
  []
  *environment*)

(defn add-option!
  [option value]
  )
