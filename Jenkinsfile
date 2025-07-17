pipeline {
    agent any

    environment {
        DB_HOST = 'localhost'
        DB_NAME = 'myapp'
        DB_USER = 'jenkins'
        DB_PASS = 'jenkins123'
        UPLOAD_PATH = '/home/ec2-user/uploads'
        KAKAO_ADMIN_KEY = '692a07ef5c27d034300190c55044f4ea'
        KAKAO_CLIENT_ID = 'a7e706b35a2aa6ca3bb74475951f6ec0'
        KAKAO_CLIENT_SECRET = 'TSCBuRtcqv4qteS35sAjTeE8Cv8rzFmx'
        OPEN_API_KEY = credentials('OPEN_API_KEY')
    }

    tools {
        jdk 'jdk17'
        gradle 'gradle-8.7'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/mina7038/jenkins-amore.git'
            }
        }

        stage('Build') {
            steps {
                bat './gradlew clean build'
            }
        }

        stage('Test') {
            steps {
                bat './gradlew test'
            }
        }

        stage('Deploy to AWS EC2') {
            steps {
                withCredentials([string(credentialsId: 'OPEN_API_KEY', variable: 'OPEN_API_KEY')]) {
                    bat """
                        echo Step 1: Create .env file
                        echo DB_HOST=localhost > .env
                        echo DB_NAME=myapp >> .env
                        echo DB_USER=jenkins >> .env
                        echo DB_PASS=jenkins123 >> .env
                        echo UPLOAD_PATH=/home/ec2-user/uploads >> .env
                        echo KAKAO_ADMIN_KEY=692a07ef5c27d034300190c55044f4ea >> .env
                        echo KAKAO_CLIENT_ID=a7e706b35a2aa6ca3bb74475951f6ec0 >> .env
                        echo KAKAO_CLIENT_SECRET=TSCBuRtcqv4qteS35sAjTeE8Cv8rzFmx >> .env
                        echo OPEN_API_KEY=$OPEN_API_KEY >> .env

                        echo Step 2: Send .env to EC2
                        scp -i C:/Users/jenkins/.ssh/jenkins.pem -o StrictHostKeyChecking=no .env ec2-user@54.180.247.132:/home/ec2-user/

                        echo Step 3: Send JAR to EC2
                        scp -i C:/Users/jenkins/.ssh/jenkins.pem -o StrictHostKeyChecking=no build/libs/app-0.0.1-SNAPSHOT.jar ec2-user@54.180.247.132:/home/ec2-user/

                        echo Step 4: Restart app on EC2
                        ssh -i C:/Users/jenkins/.ssh/jenkins.pem -o StrictHostKeyChecking=no ec2-user@54.180.247.132 ^
                        "pkill -f app-0.0.1-SNAPSHOT.jar || true & ^
                        set -a & source /home/ec2-user/.env & set +a & ^
                        nohup java -jar app-0.0.1-SNAPSHOT.jar > app.log 2>&1 &"
                    """
                }
            }
        }


    }
}
