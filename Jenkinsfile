pipeline {
    agent {
        docker {
            image 'maven:3.5-jdk-8'
            args '-v /var/local/maven:/var/maven'
        }
    }
    environment {
        MAVEN_CONFIG = "/var/maven/.m2"
        MAVEN_OPTS = "-Duser.home=/var/maven ${env.JAVA_OPTS}"
        JAVA_TOOL_OPTIONS = "${env.JAVA_OPTS}"
        NEXUS_URL = "${env.NEXUS_URL}"
    }
    stages {
        stage('Clean') {
            steps {
                sh 'env | sort'
                sh 'mvn --version'
                sh 'java -version'
                sh 'mvn clean'
            }
        }
        stage('Deploy'){
            steps {
                sh 'mvn deploy -Dmaven.test.skip=true -Dnexus.url=$NEXUS_URL'
                archiveArtifacts 'target/*.zip'
            }
        }
    }
}