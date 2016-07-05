# geonames-index

A utility for creating a Lucene search index based on [GeoNames] (http://www.geonames.org) data. The indexer makes use of the H2 database with the GeoNames data
created with [geonames-h2] (https://github.com/malesch/geonames-h2).

__Note__:
This project serves as a playground for tinkering with the GeoNames database
and other libs (Lucene 6, HugSQL, clojure.spec, Sierra's components lib).
Currently only the data from the `cities15000` table is indexed.

## Usage

1. Copy the H2 database into the project root or adapt accordingly the configuration for the DB location in `geonames-index.core`
2. Start a REPL, create the app system and start indexing:

```
> lein repl

user=> (use 'geonames-index.core)

user=> (in-ns 'geonames-index.core)

;; Create the system
geonames-index.core=> (def system (create-system system-config))

;; Start the system
geonames-index.core=> (alter-var-root #'system component/start)

;; Start indexing
geonames-index.core=> (index-cities15000 system)

;; Stop the system
geonames-index.core=> (alter-var-root #'system component/stop)

```


## License

Copyright © 2016 Marcus Spiegel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
