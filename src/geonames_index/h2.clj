(ns geonames-index.h2
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]))


(defrecord h2-database [db-spec]
  component/Lifecycle

  (start [component]
    (log/infof "Starting H2 database with configuration `%s`" db-spec)
    (let [conn (jdbc/get-connection db-spec)]
      (assoc component :conn conn)))

  (stop [component]
    (log/info "Stopping H2 database")
    (when-let [conn (:conn component)]
      (.close conn))
    (dissoc component :conn)))

(defn new-database [db-spec]
  (->h2-database db-spec))