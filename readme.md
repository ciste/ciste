# Ciste ("she steh")

[![Build Status](https://secure.travis-ci.org/duck1123/ciste.png)](http://travis-ci.org/duck1123/ciste)

Ciste is a Lojban word meaning:

x1 (mass) is a system interrelated by structure x2 among components x3
(set) displaying x4 (ka).

-- http://vlasisku.lojban.org/ciste

## About Ciste

Ciste attempts to provide a structure to Clojure applications. The
main idea revolves around matching a request to a action variable and
a pair of format and serialization parameters.

The action is simply a function that accepts a request and returns
either: a record of some sort (or a sequence of them); or nil.

The response of the action method is then passed to the apply-view
family of multimethods. The view will match the combination of action
(expressed as a var) and the format (a keyword). The view will return
a logically true value (generally a hash). If there is no view defined
for both the action and the format, a view will be searched for using
only the format. This allows generic handlers to be defined for a
format (eg, converting the structure to JSON).

The response from the view is then passed to the formatter. This
allows extra transformations to be applied for the given format. An
example of this would be converting a series of vectors into HTML
using Hiccup.

Finally, the response is formatted according to the serialization
type. This framework has been used to respond to request
simultaneously over an HTTP and XMPP connection. This allows any final
transformations to be applied (eg, adding common headers).

In addition to the main thread of request and response, triggers can
also be defined. When a matching action is invoked, the request and
the response from the action are sent to the specified
trigger. Triggers are for side effects only.

### Example

``` clojure
(ns my-app.core
  (:use ciste.core))

(defaction index
  [options]
  (index-db-method :limit (:limit options)))

(deffilter #'index :html
  [action request]
  (action {:limit (-> request :params :limit)}))

(defview #'index :html
  [records]
  {:body (show-list records)})

(defsection show-list [RecordModel]
  [records]
  [:ul (map show-list-item records)])
```

## Initializers


## Installation

Add the following to your project.clj dependencies

    [ciste "0.3.0-SNAPSHOT"]

## License

Copyright (C) 2011 KRONK Ltd.

Distributed under the Eclipse Public License, the same as Clojure.
