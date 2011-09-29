(ns ciste.debug
  (:use (clojure [pprint :only (pprint)]))
  (:require (clojure.tools [logging :as log])))

(defmacro spy
  [sym]
  `(let [value# ~sym]
     (log/info (str ~(str sym) ": "
                    (with-out-str
                      (pprint value#))))
     value#))
