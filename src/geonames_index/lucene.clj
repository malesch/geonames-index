(ns geonames-index.lucene
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.string :as string])
  (:import [java.nio.file Paths]
           [org.apache.lucene.analysis.standard StandardAnalyzer]
           [org.apache.lucene.document Document Field$Store StringField DoublePoint LongPoint NumericDocValuesField]
           [org.apache.lucene.index IndexWriter]
           [org.apache.lucene.index IndexWriterConfig]
           [org.apache.lucene.search SearcherManager SearcherFactory Query TopDocs TopScoreDocCollector]
           [org.apache.lucene.spatial.geopoint.document GeoPointField]
           [org.apache.lucene.store FSDirectory]))


;; General functions

(defn add-name-field [doc n]
  (when-not (string/blank? n)
    (.add doc (StringField. "name" n Field$Store/YES)))
  doc)

(defn add-alternate-names-field [doc names]
  (when names
    (doseq [n (filter (complement string/blank?) (vals names))]
      (.add doc (StringField. "alternate-name" n Field$Store/YES))))
  doc)

(defn add-location-fields [doc {:keys [lat lon]}]
  (when (and lon lat)
    (.add doc (GeoPointField. "location" lat lon Field$Store/YES)))
  doc)

(defn add-country-code [doc country]
  (when-not (string/blank? country)
    (.add doc (StringField. "country" country Field$Store/YES)))
  doc)

(defn add-population-count [doc population]
  (when (pos? population)
    (.add doc (LongPoint. "population" (long-array [population]))))
  doc)

(defn add-geonames-classifications [doc {:keys [class code admin1 admin2 admin3 admin4]}]
  (.add doc (StringField. "class" class Field$Store/NO))
  (.add doc (StringField. "code" code Field$Store/NO))
  (when-not (string/blank? admin1)
    (.add doc (StringField. "admin1" admin1 Field$Store/NO)))
  (when-not (string/blank? admin2)
    (.add doc (StringField. "admin2" admin2 Field$Store/NO)))
  (when-not (string/blank? admin3)
    (.add doc (StringField. "admin3" admin3 Field$Store/NO)))
  (when-not (string/blank? admin4)
    (.add doc (StringField. "admin4" admin4 Field$Store/NO)))
  doc)

(defn add-geohash [doc {:keys [class code admin1 admin2 admin3 admin4]}]
  (let [geohash (->> [admin4 admin3 admin2 admin1 code class]
                     (filter identity)
                     (interpose ".")
                     (apply str))]
    (when-not (string/blank? geohash)
      (.add doc (StringField. "geohash" geohash Field$Store/YES))))
  doc)

(defn create-document [data]
  (-> (Document.)
      (add-name-field (:name data))
      (add-alternate-names-field (:alternate-names data))
      (add-location-fields (:coordinates data))
      (add-country-code (:country data))
      (add-population-count (:population data))
      (add-geonames-classifications (:classification data))
      (add-geohash (:classification data))))

(defn build-result
  "Generate a list of [doc ID, score] tuples from the TopDocs result."
  [^TopDocs topDocs]
  (when topDocs
    (log/debugf "Max. score = %.2f" (.getMaxScore topDocs))
    (map (fn [scoredDoc] (vector (.-doc scoredDoc) (.-score scoredDoc))) (.-scoreDocs topDocs))))

;; Component functions

(defn index-document
  "Add a document to the index."
  [lucene ^Document doc]
  (when-let [writer (:writer lucene)]
    (.addDocument writer doc)))

(defn query
  "Search documents in the index."
  [lucene ^Query query]
  (let [manager (:searcher-manager lucene)
        searcher (.acquire manager)]
    (try
      (let [collector (TopScoreDocCollector/create 65535)]
        (.search searcher query collector)
        (log/debugf "Query `%s`, total hits = %s" query (.getTotalHits collector))
        (build-result (.topDocs collector)))
      (finally
        (.release manager searcher)))))


;; Component

(defn create-idx-writer-config [_]
  (let [iwc (IndexWriterConfig. (StandardAnalyzer.))]
    (.setRAMBufferSizeMB iwc 256.0)
    iwc))

(defrecord lucene [config]
  component/Lifecycle

  (start [component]
    (log/infof "Starting Lucene with index `%s`" (:index config))
    (let [dir (FSDirectory/open (Paths/get (:index config) (into-array [""])))
          iwc (create-idx-writer-config config)
          writer (IndexWriter. dir iwc)]
      ;; commit directory structure so SearchManager is happy
      (.commit writer)
      (-> component
          (assoc :writer writer)
          (assoc :searcher-manager (SearcherManager. dir (SearcherFactory.))))))

  (stop [component]
    (log/infof "Stopping Lucene")
    (when-let [writer (:writer component)]
      (.forceMerge writer 1)
      (.commit writer)
      (.close writer))
    (-> component
        (assoc :writer nil)
        (assoc :searcher-manager nil))))

(defn new-lucene [config]
  (->lucene config))