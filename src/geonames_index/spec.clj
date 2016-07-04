(ns geonames-index.spec
  (:require [clojure.spec :as s]))

(s/def ::name string?)
(s/def ::alternate-names (s/nilable (s/map-of keyword? ::name)))
(s/def ::country (s/and string? #(= (count %) 2)))
(s/def ::population integer?)

(s/def ::lon double?)
(s/def ::lat double?)
(s/def ::coordinates (s/keys :req-un [::lon ::lat]))

(s/def ::class (s/and string? #(= (count %) 1)))
(s/def ::code (s/and string? #(< (count %) 11)))
(s/def ::admin1 string?)
(s/def ::admin2 (s/nilable string?))
(s/def ::admin3 (s/nilable string?))
(s/def ::admin4 (s/nilable string?))
(s/def ::classification (s/keys :req-un [::class ::code ::admin1 ::admin2 ::admin3 ::admin4]))

(s/def ::location (s/keys :req-un [::name ::alternate-names ::coordinates ::country ::population ::classification]))
