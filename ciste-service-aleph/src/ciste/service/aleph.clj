(ns ciste.service.aleph
  (:use [aleph.http :only [start-http-server]]
        [ciste.config :only [config describe-config]])
  (:require [clojure.string :as string]))

(def ^:dynamic *future-web* (ref nil))

(describe-config [:http :port] :number
  "The port the http server should run on")

(describe-config [:http :websocket] :boolean
  "Should websocket support be enabled?")

(describe-config [:http :handler] :string
  "A string pointing to a fully namespace-qualified http handler")

(defn start
  "Start a http server"
  []
  (let [handler (config :http :handler)]
    ;; Require handler namespace
    (-> handler (string/split #"/")
        first symbol require)

    ;; start server
    (let [handler-var (resolve (symbol handler))
          stop-function (start-http-server handler-var
                                           {:port (config :http :port)
                                            :websocket (config :http :websocket)
                                            :join? false})]
      (dosync
       (ref-set *future-web* stop-function)))))

(defn stop
  "Stop http server"
  []
  (@*future-web*))
