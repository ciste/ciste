#!groovy

// Set build properties
properties([[$class: 'GithubProjectProperty',
               displayName: 'Ciste',
               projectUrlStr: 'https://github.com/duck1123/ciste/']]);

node {
    stage 'Prepare environment'

    def clojure = docker.image('clojure')
    clojure.pull()

    sh 'env | sort'

    stage 'Unit Tests'

    clojure.inside {
        checkout scm
        wrap([$class: 'AnsiColorBuildWrapper']) {
            sh 'lein midje'
        }
        step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
    }

    stage 'Generate Reports'

    clojure.inside {
        checkout scm
        sh 'lein doc'
        step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
        step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
    }

    // TODO: Skip for features and PRs
    // stage 'Deploy Artifacts'
    // sh 'lein deploy'
    
    stage 'Set Status'
    step([$class: 'GitHubCommitStatusSetter'])
}
