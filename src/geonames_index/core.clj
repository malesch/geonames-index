(ns geonames-index.core
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [geonames-index.lucene :as lucene]
            [geonames-index.h2 :as h2])
  (:import [org.apache.lucene.document Document Field$Store StringField DoublePoint LongPoint NumericDocValuesField]))

(def example-config {:db {:classname   "org.h2.Driver"
                          :subprotocol "h2"
                          :subname     "~/no-backup/geonames"
                          :user        "sa"
                          :password    ""}
                     :lucene {:index "./index"}})

(defn add-name-field [doc n]
  (when-not (string/blank? n)
    (.add doc (StringField. "name" n Field$Store/YES))))

(defn add-alternate-names-field [doc names]
  (when names
    (doseq [n (filter (complement string/blank?) names)]
      (.add doc (StringField. "alternate-name" n Field$Store/YES)))))

(defn add-location-fields [doc {:keys [lon lat]}]
  (when (and lon lat)
    (.add doc (DoublePoint. "lon" lon))
    (.add doc (NumericDocValuesField. "lon" (Double/doubleToRawLongBits lon)))
    (.add doc (DoublePoint. "lat" lat))
    (.add doc (NumericDocValuesField. "lat" (Double/doubleToRawLongBits lat)))))

(defn add-country-code [doc country]
  (when-not (string/blank? country)
    (.add doc (StringField. "country" country Field$Store/YES))))

(defn add-population-count [doc population]
  (when-not population
    (.add doc (LongPoint. "population" population))))

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
    (.add doc (StringField. "admin4" admin4 Field$Store/NO))))

(defn add-geohash [doc {:keys [class code admin1 admin2 admin3 admin4]}]
  (let [geohash (->> [admin4 admin3 admin2 admin1 code class]
                     (filter identity)
                     (interpose ".")
                     (apply str))]
    (when-not (string/blank? geohash)
      (.add doc (StringField. "geohash" geohash Field$Store/NO)))))

(defn create-document [data]
  (-> (Document.)
      (add-name-field (:name data))
      (add-alternate-names-field (:alternate-names data))
      (add-location-fields (:location data))
      (add-country-code (:country data))
      (add-population-count (:population data))
      (add-geonames-classifications (:classification data))
      (add-geohash (:classification data))))


(defn config-logging! []
  (log/merge-config! {:level :info}))

(defn create-system [config]
  (component/system-map
    :config config
    :db (h2/new-database (:db config))
    :lucene (lucene/new-lucene (:lucene config))))
