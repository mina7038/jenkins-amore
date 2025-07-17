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
                bat """
                echo "📝 1. .env 파일 생성"
                echo DB_HOST=${DB_HOST} > .env
                echo DB_NAME=${DB_NAME} >> .env
                echo DB_USER=${DB_USER} >> .env
                echo DB_PASS=${DB_PASS} >> .env
                echo UPLOAD_PATH=${UPLOAD_PATH} >> .env
                echo KAKAO_ADMIN_KEY=${KAKAO_ADMIN_KEY} >> .env
                echo KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} >> .env
                echo KAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET} >> .env
                echo OPEN_API_KEY=${OPEN_API_KEY} >> .env

                echo "📤 2. .env 파일 EC2로 전송"
                scp -i ~/.ssh/jenkins.pem -o StrictHostKeyChecking=no .env ec2-user@54.180.247.132:/home/ec2-user/

                echo "🚀 3. JAR 파일 전송"
                scp -i ~/.ssh/jenkins.pem -o StrictHostKeyChecking=no build/libs/app-0.0.1-SNAPSHOT.jar ec2-user@54.180.247.132:/home/ec2-user/

                echo "🔁 4. EC2에서 앱 재시작"
                ssh -i ~/.ssh/jenkins.pem -o StrictHostKeyChecking=no ec2-user@54.180.247.132 << EOF
                    pkill -f app-0.0.1-SNAPSHOT.jar || true

                    # 환경변수 로드
                    set -a
                    source /home/ec2-user/.env
                    set +a

                    nohup java -jar app-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
                EOF
                """

            }
        }
    }
}
