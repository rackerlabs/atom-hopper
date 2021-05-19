pipeline {
    agent any

    triggers {
        githubPush()
      }
    tools {
        maven 'MavenTest'
    }
    stages {
        stage ('Test') {
            steps {
               sh 'mvn initialize test'
            }
        }
    }
}