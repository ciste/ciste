(ns ciste.formats)

(defmulti format-as (fn [format handler request] format))
