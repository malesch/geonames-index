(ns dev
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require [com.stuartsierra.component :as component]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [geonames-index.core :as core]))

(def dev-config {:db {:classname   "org.h2.Driver"
                      :subprotocol "h2"
                      :subname     "~/no-backup/geonames"
                      :user        "sa"
                      :password    ""}
                 :lucene {:index "./index"}})

(def system (core/create-system dev-config))


(defn init
  "Initialization of the system"
  []
  ;; TODO
  )

(defn start
  "Starts the system"
  []
  (alter-var-root #'system component/start))

(defn stop
  "Stops the system"
  []
  (alter-var-root #'system component/stop))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after `go))
