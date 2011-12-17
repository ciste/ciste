{:development
 {
  :print {
          :predicates false
          }
  :triggers {:thread-count 1}

  }

 :test

 {
  :print {
          :actions    true
          :predicates false
          }
  :run-triggers true
  :triggers {:thread-count 1}
  :use-pipeline true
  }
 }
