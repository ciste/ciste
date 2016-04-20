(ns ciste.formats
  (:use [ciste.core :only [*format*]])
  (:require [taoensso.timbre :as timbre]))

(defmulti format-as (fn [format _handler _request] *format*))
