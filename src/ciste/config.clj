(ns ciste.config)

(defonce ^:dynamic *environment* :development)

(defonce ^:dynamic *environments* (ref {}))

(defn config
  ([]
     (get @*environments* *environment*))
  ([& ks]
     (get-in (config) ks)))

(defn load-config
  ([] (load-config "config.clj"))
  ([filename]
     (->> filename
          slurp
          read-string
          (ref-set *environments*)
          dosync)))

(defn environment
  []
  *environment*)

(defn add-option!
  [option value]
  )
