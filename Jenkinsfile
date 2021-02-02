pipeline {
    agent any

    triggers {
        githubPush()
      }
    tools {
        maven 'MavenTest'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh 'mvn clean'
            }
        }

        stage ('Build') {
            steps {
               sh 'mvn initialize test'
            }
        }
    }
}