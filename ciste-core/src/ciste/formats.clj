(ns ciste.formats)

(defmulti format-as (fn [format _handler _request] format))
