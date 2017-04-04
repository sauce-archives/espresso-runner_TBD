pipeline {
  agent {
    docker 'maven:3.3.9'
  }
  stages {
    stage("Get APKs") {
      steps {
        sh 'wget https://github.com/moizjv/spoon-samples/raw/master/espresso-sample/app-debug.apk'
        sh 'wget https://github.com/moizjv/spoon-samples/raw/master/espresso-sample/app-debug-androidTest-unaligned.apk'
      }
    }
    stage("Build") {
        steps {
          try {
            sh 'mvn package'
          } finally {
            junit '**/build/test-reports/*.xml'
          }
        }
    }
    stage("Run test"){
      steps {
        try {
          sh 'java -jar espresso-runner.jar'
        } finally {
          junit '*.xml'
        }
      }
    }
  }
}