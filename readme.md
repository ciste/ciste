# Ciste ("she steh")

Ciste is a Lojban word meaning:

x1 (mass) is a system interrelated by structure x2 among components x3
(set) displaying x4 (ka).

-- http://vlasisku.lojban.org/ciste

## About Ciste

Ciste attempts to provide a structure to Clojure applications. The
main idea revolves around matching a request to a action variable and
a pair of format and serialization parameters.

The action is simply a function that accepts a request and returns
either a record of some sort (or a sequence of), or nil.

The response of the action method is then passed to the apply-view
family of multimethods. The view will match the combination of action
(expressed as a var) and the format (a keyword). The view will return
a logically true value. (generally a hash) If there is no view defined
for both the action and the format, a view will be searched for using
only the format. This allows generic handlers to be defined for a
format. (ie. converting the structure to JSON)

The response from the view is then passed to the formatter. This
allows extra transformations to be applied for the given format. An
example of this would be converting a series of Vectors into HTML
using Hiccup.

Finally, the response is formatted according to the serialization
type. This framework has been used to respond to request
simultaneously over an HTTP and XMPP connection. This allows any final
transformations to be applied. (ie. adding common headers)

In addition to the main thread of request and response, triggers can
also be defined. When a matching action is invoked, the request and
the response from the action are sent to the specified
trigger. Triggers are for side effects only.

### Usage

    (ns my-app.core
      (:use ciste.core))
    
    (defaction index
      [request]
      (index-db-method))
    
    (defview #'index :html
      [records]
      {:body (show-list records)})
    
    (defsection show-list [RecordModel]
      [records]
      [:ul (map show-list-item records)])

## Sections

Sections are a series of multimethods for generically transforming
records 

## Triggers

Triggers allow you to have functions called as part of a seperate
thread pool whenever a matching action is invoked.

A Trigger is a function that takes 3 arguments: The action, the
request map, and the response from invoking the action.

All of the dynamic bindings from the original request are coppied to
the trigger.

### Usage

    (defaction my-action
      [request]
      {:foo 23, :bar 42})
      
    (defn my-trigger
      [action request record]
      "Do something in a different thread")
      
    (ciste.trigger/add-trigger! #'my-action #'my-trigger)

## Factories

This section may be removed in a future release.

Factories allow test data to be easily produced. The goal was to have
something similar to Factory Girl for Clojure.

A factory is defined as such: 

    (defseq :word
      [n]
      (str "word" n))
    
    (deffactory User
      (let [password (fseq :word)]
        {:username (fseq :word)
         :domain (-> (config) :domain)
         :name (fseq :word)
         :first-name (fseq :word)
         :last-name (fseq :word)
         :password password
         :confirm-password password}))

And used like:

    (fseq :word)
    (factory User)
    (factory User {:name "tom"})

## Installation

    mvn install

## License

Copyright (C) 2011 KRONK Ltd.

Distributed under the Eclipse Public License, the same as Clojure.
