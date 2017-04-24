pipeline {
    agent any

    stages {
        stage('Compiling') {
            steps {
                gradle {
                    useWrapper true
                    tasks "classes"
                    tasks "testClasses"
                }
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Analyzing') {
            steps {
                sh './gradlew sonarqube'
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