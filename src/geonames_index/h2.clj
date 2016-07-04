(ns geonames-index.h2
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [hikari-cp.core :as hikari]
            [clojure.java.jdbc :as jdbc]))

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

;; Component functions

(defn do-locations [db row-fn]
  (jdbc/with-db-connection [conn db]
                           (let [rows (jdbc/query conn "SELECT * FROM cities15000")]
                             (doseq [row rows]
                               (row-fn row)))))

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