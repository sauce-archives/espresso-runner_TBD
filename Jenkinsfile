pipeline {
  agent {
    docker 'maven:3.3.9'
  }
  environment {
    APP=app.apk
    TEST=test.apk
  }
  properties(
    [
      parameters([
        string(defaultValue: '1', description: '', name: 'SUITE'),
        string(defaultValue: 'testobject', description: '', name: 'USER_NAME'),
        string(defaultValue: '', description: '', name: 'PASSWORD'),
        string(defaultValue: 'espresso-runner-test', description: '', name: 'PROJECT')
      ])
    ]
  )
  stages {
    stage("Get APKs") {
      steps {
        sh 'wget -O $APP https://github.com/moizjv/spoon-samples/raw/master/espresso-sample/app-debug.apk'
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
      steps {
        sh 'java -jar $(ls target/espressorunner*jar)'
      }
      post {
        always {
          junit '*.xml'
        }
      }
    }
  }
}