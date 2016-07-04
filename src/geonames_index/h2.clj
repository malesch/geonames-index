(ns geonames-index.h2
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [hikari-cp.core :as hikari]
            [hugsql.core :as hugsql]
            [clojure.string :as string]))

;; Example DB spec
;
; {:adapter  "h2"
;  :url      "jdbc:h2:file:~/no-backup/geonames"
;  :username "sa"
;  :password ""
;  ;; Pool configuration (defaults examples)
;  ;; See: https://github.com/tomekw/hikari-cp#configuration-options
;  :auto-commit true
;  :connection-timeout 30000
;  :idle-timeout 600000
;  :minimum-idle 10
;  :maximum-pool-size 10
;  :register-mbeans false}

;; HugSQL DB functions
(hugsql/def-db-fns "sql/geonames.sql")


;; General functions


;; Component functions

(defn fetch-alternate-names
  "Return the alternate names for a geonameid as a map and with 2-letter keyword locales as key."
  [db geonameid]
  (let [names (alternate-names-by-geonameid db {:id geonameid})
        nmap (->> names
                  (map (juxt :isolanguage :alternatename))
                  (filter (fn [[k _]] (= (count k) 2)))
                  (map (fn [[k v]] [(keyword k) v]))
                  (into {}))]
    (when-not (empty? nmap)
      nmap)))

(defn do-cities15000-locations [db row-fn]
  (let [rows (all-cities15000 db)]
    (for [row rows]
      {:name            (:name row)
       :locations       {:lon (float (:long row))
                         :lat (float (:lat row))}
       :country         (:country row)
       :population      (:population row)
       :classification  {:class  (:fclass row)
                         :code   (:fcode row)
                         :admin1 (:admin1 row)
                         :admin2 (:admin2 row)
                         :admin3 (:admin3 row)
                         :admin4 (:admin4 row)}
       :alternate-names (fetch-alternate-names db (:id row))})))

;; Component

(defrecord h2-database [db-spec]
  component/Lifecycle

  (start [component]
    (log/infof "Starting H2 database with spec `%s`" db-spec)
    (let [ds (hikari/make-datasource db-spec)]
      (assoc component :datasource ds)))

  (stop [component]
    (log/info "Stopping H2 database")
    (when-let [ds (:datasource component)]
      (hikari/close-datasource ds))
    (assoc component :datasource nil)))

(defn new-database [db-spec]
  (->h2-database db-spec))