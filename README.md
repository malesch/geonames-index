# geonames-index

A utility for creating a Lucene search index based on the [GeoNames] (http://www.geonames.org) data. The indexer makes use of the H2 database with the GeoNames data
created with [geonames-h2] (https://github.com/malesch/geonames-h2).

__REMARK__:
___
This projects is only a playground for experimenting with the GeoNames database
and other libs (Lucene 6, HugSQL, clojure.spec, Sierra's components lib).
Currently only the data from the `cities15000` table is indexed. The alternate
names are read from the table `alternatenames` but as the language information
is not indexed, processing would be much more efficient by simply slurping in
the original feed file (reading from the H2 database is not done not very
efficiently and is therefore slow). <br/>
The intention for using the database with the GeoNames data is to be more
flexible with processing, for instance to reconstruct and index the location
hierarchies.
___

## Usage

1. Copy the H2 database into the project root or adapt accordingly the configuration for the DB location in `geonames-index.core`
2. Start a REPL, create the app system and start indexing:
```
> lein repl

> ;; Create the system
> (def system (create-system config))

> ;; Start the system
> (alter-var-root #'system component/start)

> ;; Start indexing
> (index-cities15000 system)

> ;; Stop the system
> (alter-var-root #'system component/stop)

```

## TODOS

* Example queries (e.g. spatial queries)
* Storing of hierarchy information (location references)
* Parallelization of indexing process
* Revision of SQL statements
* Additional attributes (geohash, 3-letter country code,...?)
* Additional data sources/tables
* ...


## License

Copyright © 2016 Marcus Spiegel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
