(ns geonames-index.data)

;; Example data structure extracted from the database

(def demo-data {:name "Milano"
                :alternate-names {:de "Mailand"
                                  :es "Milán"
                                  :ar "ميلانو"
                                  :bg "Милано"
                                  :fr "Milan"
                                  :hu "Milánó"
                                  :zh-TW "米蘭"
                                  :ja "ミラノ"
                                  :ta "மிலன்"
                                  :ug "مىلان"
                                  :scn "Milanu"}
                :location {:lon 9.1895100
                           :lat 45.4642700}
                :country "IT"
                :population 1236837
                :classifications {:class "P"
                                  :code "PPLA"
                                  :admin1 "09"
                                  :admin2 "MI"
                                  :admin3 "015146"
                                  :admin4 nil}})