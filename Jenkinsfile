pipeline {
  agent {
    docker 'maven:3.3.9'
  }
  environment {
    APP='app.apk'
    TEST='test.apk'
  }
  stages {
    stage("Get APKs") {
      steps {
        sh 'wget -O ${env.APP} https://github.com/moizjv/spoon-samples/raw/master/espresso-sample/app-debug.apk'
        sh 'wget -O $TEST https://github.com/moizjv/spoon-samples/raw/master/espresso-sample/app-debug-androidTest-unaligned.apk'
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
      environment {
        SUITE=params.SUITE
        USER_NAME=params.USER_NAME
        PASSWORD=params.PASSWORD
        PROJECT=params.PROJECT
      }
      steps {
        sh 'java -jar $(ls target/espressorunner*jar)'
      }
      post {
        failure {
          sh 'env'
        }
        always {
          junit '*.xml'
        }
      }
    }
  }
}