(ns geonames-index.core
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(defn config-logging! []
  (log/merge-config! {:level :info}))

