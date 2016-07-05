(ns geonames-index.lucene-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [geonames-index.core :refer [config-logging!]]
            [geonames-index.lucene :as l]
            [geonames-index.location :refer :all])
  (:import [org.apache.lucene.analysis.standard StandardAnalyzer]
           [org.apache.lucene.document LongPoint]
           [org.apache.lucene.index Term]
           [org.apache.lucene.search TermQuery BooleanQuery$Builder BooleanClause$Occur]
           [org.apache.lucene.spatial.geopoint.search GeoPointDistanceQuery]
           [org.apache.lucene.util QueryBuilder]))

;; Lucene test configuration
(def config {:index "target/index"})

;; Test location data
(def locations [;; 2657896
                {:name            "Zurich"
                 :alternate-names {:de "Zürich"
                                   :it "Zurigo"
                                   :la "Turicum"
                                   :ta "Tagalog"}
                 :coordinates     {:lat 47.36667
                                   :lon 8.55}
                 :country         "CH"
                 :population      341730
                 :classification  {:class  "P"
                                   :code   "PPLA"
                                   :admin1 "ZH"
                                   :admin2 "112"
                                   :admin3 "261"
                                   :admin4 nil}}
                ;; 2661604
                {:name            "Basel"
                 :alternate-names {:de "Basel"
                                   :it "Basilea"
                                   :eo "Bazelo"
                                   :lv "Bāzele"
                                   :zh "巴塞尔"}
                 :coordinates     {:lat 47.5584
                                   :lon 7.57327}
                 :country         "CH"
                 :population      164488
                 :classification  {:class  "P"
                                   :code   "PPLA"
                                   :admin1 "BS"
                                   :admin2 "1200"
                                   :admin3 "2701"
                                   :admin4 nil}}
                ;; 2660971
                {:name            "Dübendorf"
                 :alternate-names {:de "Duebendorf"}
                 :coordinates     {:lat 47.39724
                                   :lon 8.61872}
                 :country         "CH"
                 :population      19882
                 :classification  {:class  "P"
                                   :code   "PPL"
                                   :admin1 "ZH"
                                   :admin2 "109"
                                   :admin3 "191"
                                   :admin4 nil}}])

(defn create-test-index []
  (log/info "Create test index")
  (let [lucene (component/start (l/new-lucene config))]
    (try
      (doseq [loc locations]
        (->> loc
             (l/create-document)
             (l/index-document lucene)))
      (finally
        (component/stop lucene)))))

(defn delete-test-index []
  (log/info "Delete test index")
  (let [lucene (component/start (l/new-lucene config))]
    (try
      (l/delete-all lucene)
      (finally
        (component/stop lucene)))))

(defn prepare-index-fixture [f]
  (config-logging! :error)
  (create-test-index)
  (f)
  (delete-test-index))

(use-fixtures :once prepare-index-fixture)

;; Tests

(deftest test-code-query
  (let [lucene (component/start (l/new-lucene config))
        query (TermQuery. (Term. "code" "PPL"))]
    (try
      (let [res (l/query lucene query)]
        (is (and (= (count res) 1)
                 (= (ffirst res) 2))))
      (finally
        (component/stop lucene)))))

(deftest test-name-query
  (let [lucene (component/start (l/new-lucene config))
        name-query (fn [n]
                     (let [qb (QueryBuilder. (StandardAnalyzer.))]
                       (.build
                         (doto (BooleanQuery$Builder.)
                           (.add (.createBooleanQuery qb "name" n) BooleanClause$Occur/SHOULD)
                           (.add (.createBooleanQuery qb "alternate-name" n) BooleanClause$Occur/SHOULD)))))]
    (try
      (is (= 0 (count (l/query lucene (name-query "foo")))))
      (is (= 1 (count (l/query lucene (name-query "zurigo")))))
      (is (= 1 (count (l/query lucene (name-query "zürich")))))

      (finally
        (component/stop lucene)))))

(deftest test-distance-query
  ;; Winterthur (lat/lon): 47.50564 / 8.72413
  ;;   Distance Zurich:    ~ 20260 m
  ;;            Basel:     ~ 86600 m
  ;;            Dübendorf: ~ 14430 m
  (let [lucene (component/start (l/new-lucene config))]
    (try
      (is (empty? (l/query lucene (GeoPointDistanceQuery. "location" 47.50564 8.72413 10000))))
      (is (= 1 (count (l/query lucene (GeoPointDistanceQuery. "location" 47.50564 8.72413 20000)))))
      (is (= 2 (count (l/query lucene (GeoPointDistanceQuery. "location" 47.50564 8.72413 21000)))))
      (is (= 2 (count (l/query lucene (GeoPointDistanceQuery. "location" 47.50564 8.72413 86000)))))
      (is (= 3 (count (l/query lucene (GeoPointDistanceQuery. "location" 47.50564 8.72413 87000)))))
      (finally
        (component/stop lucene)))))

(deftest test-population-range-query
  (let [lucene (component/start (l/new-lucene config))]
    (try
      (is (empty? (l/query lucene (LongPoint/newRangeQuery "population" 100 19881))))
      (is (= 1 (count (l/query lucene (LongPoint/newRangeQuery "population" 100 164487)))))
      (is (= 2 (count (l/query lucene (LongPoint/newRangeQuery "population" 100 341729)))))
      (is (= 3 (count (l/query lucene (LongPoint/newRangeQuery "population" 100 341730)))))
      (finally
        (component/stop lucene)))))