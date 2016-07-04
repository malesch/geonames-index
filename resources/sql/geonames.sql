
-- :name all-cities15000
-- :doc Return all entries from the cities15000 table
SELECT * FROM cities15000

-- :name alternate-names-by-geonameid :?
-- :doc Return all alternate names for a geonameid with a 2-letter language code
SELECT isolanguage,alternatename FROM alternatenames WHERE geonameid=:id
