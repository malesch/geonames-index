(ns geonames-index.core
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [geonames-index.lucene :as lucene]
            [geonames-index.h2 :as h2]))

(def example-config {:db {:classname   "org.h2.Driver"
                          :subprotocol "h2"
                          :subname     "~/no-backup/geonames"
                          :user        "sa"
                          :password    ""}
                     :lucene {:index "./index"}})

(defn config-logging! []
  (log/merge-config! {:level :info}))

(defn create-system [config]
  (component/system-map
    :config config
    :db (h2/new-database (:db config))
    :lucene (lucene/new-lucene (:lucene config))))
