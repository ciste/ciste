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

### Example

    (defaction login
      [username password]
      ;; Perform authentication
      )
    
    (deffilter #'login :http
      [action request]
      (let [{{:keys [username password]} :params}]
        (action username password)))


## Views

A View is a pair of multi-methods: apply-view, and default-format. The
apply-view method dispatches on a vector containing the Action and the
Format. If no match is found this value, then default-format tries
using only Format.

A View accepts two parameters: the request, and the response from
invoking the action. A View should render the supplied data into a
structure appropriate to the Format. It is not required, but this is
most commonly a map.

### Example

    (defaction show
      [id]
      (fetch-user id))
    
    (deffilter #'show :http
      [action {{id :id} :params}]
      (action id))
    
    (defview #'show :html
      [request user]
      {:status 200
       :body [:div.user
               [:p (:name user)]]})

## Config

Ciste uses the config function in ciste.config to perform all the
configuration. Config takes a variable number of key values and will
either return a non-nil value if that option is defined, or will raise
an exception if it is not.

The config information is read from the file "config.clj" at the base
of the project's directory. The config file should contain a hash-map.

The top-level keys will be the names of environments. The values of
these keys will be an arbitrarily complex structure of hashes,
vectors, and other data.

### Example

config.clj

    {:default {:option1 "foo"
               :option2 {:value "bar" :title "BAR"}
               :option3 ["foo" "bar" "baz"]}}

    > (use 'ciste.config)
    > (load-config)
    > (set-environment! :default)
    > (config :option1) => "foo"
    > (config :option3) => ["foo" "bar" "baz"]
    > (config :option2 :title) => "BAR"

## Initializers

Initializers are blocks of code that need to set up the environment of
the namespace, but cannot run until the configuration system is
available with a valid environment.

Whenever the environment is changed, the initializers will run in the
order they were declared.

Note: At this time, Initializers will be re-run if the namespace is
reloaded. For this reason, it is recommended that initializers be able
to handle being run multiple times gracfully.

### Example

    (ns ciste.example
      (:use [ciste.config :only (definitializer)]))
    
    (definitializer
      (println "This will be run when the environment is set")
      (println (config :hostname)))
    
    (println "out of the initializer"


    > (use 'ciste.example)
    out of the initializer
    > (set-environment! :development)
    This will be run when the environment is set
    server1.example.com

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

## Workers

Workers are tasks that will repeatedly run. A worker can be started
and stopped by any thread. When a worker is stopped, it will continue
until the next time that it exits. You can check if it's stopping
within your code if you wish to exit earlier.

### Example

    (defworker :queue-checker
      [queue-name]
      (check-and-process-queue queue-name))
    
    (start-worker! :queue-checker)
    (stop-worker! :queue-checker worker-id)
    (stop-all-workers!)

## Debug

The ciste.debug namespace contains only a single macro: spy. Spy will
log the code it wraps as well as a pretty-printed version of it's
value. That value will then be returned. This allows you to easily
monitor any bit of code by simply wrapping it with spy.

## Installation

Add the following to your project.clj dependencies

    [ciste "0.2.0-SNAPSHOT"]

## License

Copyright (C) 2011 KRONK Ltd.

Distributed under the Eclipse Public License, the same as Clojure.
