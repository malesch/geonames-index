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

;; Create and start the system
geonames-index.core=> (def system (component/start-system (create-system system-config)))

;; Start indexing
geonames-index.core=> (index-cities15000 system)

;; Stop the system
geonames-index.core=> (component/stop-system system)

```


## License

Copyright © 2016 Marcus Spiegel

Permission to use, copy, modify, and distribute this software for any purpose with or without fee is hereby granted, provided that the above notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
