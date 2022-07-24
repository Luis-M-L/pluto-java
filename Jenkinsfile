pipeline {
    agent any

    tools {
        maven 'maven-3.8.1'
    }
    stages {

        stage ("build") {

            steps {
                echo 'Building pluto...'
                sh 'mvn clean package'
            }
        }

        stage ("deploy") {

            steps {
                echo 'Deploying pluto version...'
            }
        }
    }
}