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

### Example

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

## Actions

Actions are simply functions. An Action can take any number of
parameters and should return any logically true value if the action
succeeded.

An Action is responsible for carrying out the core operations relating
to the request. If any resources are changed, it should happen here.

While any function will work as an action, it is recommended that you
use the defaction macro to define your Action. This serves to both
clearly identify the Actions in the code, but it may be necessary to
add custom metadata to actions in the future.

## Filters

Filters are methods of the multimethod apply-filter. A Filter
dispatches on the Action and the Serialization. A Filter takes 2
arguments: The Action, and the request map.

It is the job of the Filter to parse the request map and produce the
options to be passed to Action. The Filter must call that action with
the appropriate arguments.

While it is possible to modify the response from the Action, it is
recommended that filters not modify responses. (That would belong in
the view.)

## Views

A View is a pair of multi-methods: apply-view, and default-format. The
apply-view method dispatches on a vector containing the Action and the
Format. If no match is found this value, then default-format tries
using only Format.

A View accepts two parameters: the request, and the response from
invoking the action. A View should render the supplied data into a
structure appropriate to the Format. It is not required, but this is
most commonly a map.

## Sections

Sections are a series of multimethods for generically transforming
records into the most appropriate format.

A Section dispatches on a Vector containing the type of the first
argument or the type of the first element of the first argument if the
Section has been defined as a :seq type, the Format, and
the Serialization. If no match is found, the final value is removed
and tried again. This repeats until there is only the type.

### Example

    (declare-section show-section)
    (declare-section index-section :seq)

    (defsection show-section [User :html :http]
      [user & options]
      [:div
        [:p "Name: " (:name user)]
        [:p "Email: " (:email user)]])
    
    (defsection index-section [User :html :http]
      [users & options]
      [:ul
        (map
          (fn [user]
            [:li (show-section user)])
        users)])

## Triggers

Triggers allow you to have functions called as part of a seperate
thread pool whenever a matching action is invoked.

A Trigger is a function that takes 3 arguments: The action, the
request map, and the response from invoking the action.

All of the dynamic bindings from the original request are coppied to
the trigger.

### Example

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

### Example

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

## Debug

The ciste.debug namespace contains only a single macro: spy. Spy will
log the code it wraps as well as a pretty-printed version of it's
value. That value will then be returned. This allows you to easily
monitor any bit of code by simply wrapping it with spy.

## Installation

    mvn install

## License

Copyright (C) 2011 KRONK Ltd.

Distributed under the Eclipse Public License, the same as Clojure.
