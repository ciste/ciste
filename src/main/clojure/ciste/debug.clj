(ns ciste.debug
  (:use clojure.pprint
        [clojure.contrib.logging :only (info)]))

(defmacro spy
  [sym]
  `(let [value# ~sym]
     (info (with-out-str (print (str ~(str sym) ": "))
        (pprint value#)))
     value#))
