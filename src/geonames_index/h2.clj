(ns geonames-index.h2
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [hikari-cp.core :as hikari]
            [clojure.java.jdbc :as jdbc]
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

;; General functions

;; Component functions

(defn fetch-alternate-names
  "Return the alternate names for a geonameid as a map and with 2-letter keyword locales as key."
  [db geonameid]
  (jdbc/with-db-connection [conn db]
                           (let [names (jdbc/query conn (format "SELECT isolanguage,alternatename FROM alternatenames WHERE geonameid=%s" geonameid))
                                 nmap (->> names
                                           (map (juxt :isolanguage :alternatename))
                                           (filter (fn [[k _]] (= (count k) 2)))
                                           (map (fn [[k v]] [(keyword k) v]))
                                           (into {}))]
                             (when-not (empty? nmap)
                               nmap))))

(defn do-cities15000-locations [db row-fn]
  (jdbc/with-db-connection [conn db]
                           (let [rows (jdbc/query conn "SELECT * FROM cities15000")]
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
                                :alternate-names (fetch-alternate-names db (:id row))}))))

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