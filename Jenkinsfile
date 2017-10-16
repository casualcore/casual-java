pipeline {
    agent any

    stages {

        stage('Notify Slack')
        {
            steps {
                slackSend color: "good", message: "Build started: ${env.JOB_NAME} - ${env.BUILD_NUMBER} (<${env.JOB_URL}|Open>)"
            }
        }
        stage('Pull docker image') {
            steps {
                sh 'docker pull openjdk:8'
            }
        }

        stage('Compiling') {
            steps {
                withDockerContainer("openjdk:8") {
                    sh './gradlew classes'
                    sh './gradlew testClasses'
                }
            }
        }
        stage('Test') {
            steps {
                withDockerContainer("openjdk:8") {
                    sh './gradlew test'
                }
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
                withDockerContainer("openjdk:8") {
                    sh './gradlew build'
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying.... to something... possibly'
            }
        }
    }

    post {
       success {
         slackSend color: "good", message: "Build finished: ${env.JOB_NAME} - ${env.BUILD_NUMBER} (<${env.JOB_URL}|Open>)"
       }

       failure {
         slackSend color: "danger", message: "Build failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER} (<${env.JOB_URL}|Open>)"
       }
    }
}
