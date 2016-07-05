(ns geonames-index.core
  (:require [com.stuartsierra.component :as component]
            [clojure.spec :as s]
            [taoensso.timbre :as log]
            [geonames-index.spec :as spec]
            [geonames-index.lucene :as lucene]
            [geonames-index.h2 :as h2]))

(def system-config {:db     {:adapter  "h2"
                             :url      "jdbc:h2:file:~/no-backup/geonames"
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

(defn config-logging!
  ([]
   (config-logging! :info))
  ([level]
   (log/merge-config! {:level level})))

;; General functions

(defn index-location [lucene loc]
  (if (s/valid? ::spec/location loc)
    (do
      (log/infof "Processing GeoNameID: %s" (:id loc))
      (->> loc
           (lucene/create-document)
           (lucene/index-document lucene)))
    (log/error "Invalid location data: " + (s/explain ::spec/location loc))))

;; System functions

(defn index-cities15000 [system]
  (let [{:keys [db lucene]} system]
    (h2/process-cities15000-locations db (partial index-location lucene))))

;; System

;(defn create-system [{:keys [db lucene] :as config}]
;  (component/system-map
;    :config config
;    :db (h2/new-database db)
;    :lucene (lucene/new-lucene lucene)))

(defrecord GeonamesIndex [db lucene]
  component/Lifecycle

  (start [component]
    (config-logging!)
    (log/info "Starting GeonamesIndex")
    component)

  (stop [component]
    (log/info "Stopping GeonamesIndex")
    component))


(defn create-system [{:keys [db lucene] :as config}]
  (component/system-map
    :config config
    :db (h2/new-database db)
    :lucene (lucene/new-lucene lucene)
    :app (component/using
           (map->GeonamesIndex config)
           [:db :lucene])))


;; (def system (component/start-system (create-system system-config)))
;; (index-cities15000 system)
;; (component/stop-system system)

