(ns ciste.commands-test
  (:use (ciste commands
               [debug :only [spy]]
               [test-helper :only [test-environment-fixture]])
        midje.sweet))

(test-environment-fixture

 (fact "#'add-command!"
   (fact "should assign that var to the command map"
     (add-command! "add" #'+)

     (get @*commands* "add") => #'+))

 (fact "#'command-names"

   (command-names) => (contains ["add"])))
