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
      environment {
        MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
      }
      steps {
        sh 'mvn --batch-mode package'
      }
      post {
        always {
          junit '**/surefire-reports/*.xml'
        }
      }
    }
    stage("Run test"){
      steps {
        sh 'java -jar espresso-runner.jar'
      }
      post {
        always {
          junit '*.xml'
        }
      }
    }
  }
}