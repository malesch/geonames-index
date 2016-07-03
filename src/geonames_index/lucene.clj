(ns geonames-index.lucene
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log])
  (:import [java.nio.file Paths]
           [org.apache.lucene.analysis.standard StandardAnalyzer]
           [org.apache.lucene.document Document]
           [org.apache.lucene.index IndexWriter]
           [org.apache.lucene.index IndexWriterConfig]
           [org.apache.lucene.store FSDirectory]))

(defn index-document [indexer ^Document doc]
  (when-let [writer (:writer indexer)]
    (.addDocument writer doc)))

(defn create-idx-writer-config [_]
  (let [iwc (IndexWriterConfig. (StandardAnalyzer.))]
    (.setRAMBufferSizeMB iwc 256.0)
    iwc))

(defrecord lucene [config]
  component/Lifecycle

  (start [component]
    (log/infof "Starting Lucene with index `%s`" (:index config))
    (let [dir (FSDirectory/open (Paths/get (:index config) (into-array [""])))
          iwc (create-idx-writer-config config)]
      (assoc component :writer (IndexWriter. dir iwc))))

  (stop [component]
    (log/infof "Stopping Lucene")
    (when-let [writer (:writer component)]
      (.close writer))
    (dissoc component :writer)))

(defn new-lucene [config]
  (->lucene config))