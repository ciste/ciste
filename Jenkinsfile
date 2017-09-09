#!/usr/bin/env groovy

def org = 'ciste'
def project = 'ciste'

def buildImage

node('docker') {
  ansicolor('xterm') {
    timestamps {
      stage('Init') {
        cleanWs()

        // Set current git commit
        checkout scm

        buildImage = docker.image('clojure')
        buildImage.pull()

        sh 'env | sort'
      }

      stage('Unit Tests') {
        buildImage.inside {
          try {
            sh 'lein midje'
          } finally {
            junit 'target/surefire-reports/TEST-*.xml'
          }
        }
      }

      stage('Generate Reports') {
        buildImage.inside {
          checkout scm
          sh 'lein doc'
          step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
          step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
        }
      }

// TODO: Skip for features and PRs
//stage('Deploy Artifacts') {
//    buildImage.inside {
//        withCredentials([[$class: 'UsernamePasswordMultiBinding',
//                            credentialsId: 'repo-creds',
//                            usernameVariable: 'REPO_USERNAME', passwordVariable: 'REPO_PASSWORD']]) {
//            sh 'lein deploy'
//        }
//    }
//}



    }
  }
}
