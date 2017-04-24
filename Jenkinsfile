pipeline {
    agent any

    stages {
        stage('Compiling') {
            steps {
                sh './gradlew classes'
                sh './gradlew testClasses'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Analyzing') {
            steps {
                echo 'need to setup sonar'
                //sh './gradlew sonarqube'
            }
        }
        stage('Packaging') {
            steps {
                sh './gradlew build -xtest -xsonar'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying.... to something... possibly'
            }
        }
    }
}