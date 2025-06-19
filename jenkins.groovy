pipeline {
    agent any
    environment {
        IMAGE_NAME = "stock-ms"
    }
    stages{
        stage('Github clone repo') {
            steps {
                git branch: 'main', url: 'https://github.com/Dustbowl/stock-ms.git'
            }
        }
        stage('Maven Compile') {
            steps {
                sh "mvn clean compile"
            }
        }
        stage('Maven Build') {
            steps {
                sh "mvn clean install"
            }
        }
        stage('Docker Create Image') {
            steps {
                sh "docker logout"
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_PASS'
                )]){
                    sh '''
                        echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker build -t $DOCKERHUB_USER/${IMAGE_NAME}:$BUILD_NUMBER ./Dockerfile
                        docker push $DOCKERHUB_USER/${IMAGE_NAME}:$BUILD_NUMBER
                    '''
                }
            }
        }
    }
}