(ns geonames-index.core
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [geonames-index.lucene :as lucene]
            [geonames-index.h2 :as h2]))

(def example-config {:db     {:adapter  "h2"
                              :url      "jdbc:h2:./geonames"
                              :username "sa"
                              :password ""

                              ;; Pool configuration (defaults examples)
                              ;; See: https://github.com/tomekw/hikari-cp#configuration-options
                              :auto-commit true
                              :connection-timeout 30000
                              :idle-timeout 600000
                              :minimum-idle 10
                              :maximum-pool-size 10
                              :register-mbeans false}
                     :lucene {:index "./index"}})

(defn config-logging! []
  (log/merge-config! {:level :info}))

(defn create-system [config]
  (component/system-map
    :config config
    :db (h2/new-database (:db config))
    :lucene (lucene/new-lucene (:lucene config))))
