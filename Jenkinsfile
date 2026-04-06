pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Gradle Sanity Check') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew --no-daemon -Dorg.gradle.java.home="$JAVA_HOME" clean help'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/reports/**', allowEmptyArchive: true
        }
    }
}
