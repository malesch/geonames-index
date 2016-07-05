(defproject geonames-index "0.1.0-SNAPSHOT"
  :description "Indexing utility for GeoNames data sets."
  :url "https://github.com/malesch/geonames-index"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha8"]
                 ;; System
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/timbre "4.5.1"]
                 ;; Database
                 [com.h2database/h2 "1.4.192"]
                 [com.layerware/hugsql "0.4.7"]
                 [hikari-cp "1.7.1"]
                 ;; Lucene
                 [org.apache.lucene/lucene-core "6.1.0"]
                 [org.apache.lucene/lucene-analyzers-common "6.1.0"]
                 [org.apache.lucene/lucene-queryparser "6.1.0"]
                 [org.apache.lucene/lucene-spatial "6.1.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}})
