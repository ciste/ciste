(ns ciste.debug
  (:use (clojure [pprint :only (pprint)]))
  (:require (clojure.tools [logging :as log])))

(defmacro spy
  [sym]
  `(let [value# ~sym]
     (log/info (with-out-str (print (str ~(str sym) ": "))
        (pprint value#)))
     value#))
