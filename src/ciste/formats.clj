(ns ciste.formats
  (:use [ciste.core :only [*format*]])
  (:require [clojure.tools.logging :as log]))

(defmulti format-as (fn [format _handler _request] *format*))
