pipeline {
    agent any

    stages {
        stage('Initialize') {
            steps {
                slackSend(
                    color: '#f0544c',
                    message: "🚀 *Started:* Job ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}] (<${env.BUILD_URL}|Check Console>)"
                )
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                sh 'mvn clean'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package -DskipITs -DskipUTs -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -DskipITs'
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'mvn verify -DskipUTs'
            }
        }

        stage('Mutation tests') {
            steps {
                sh 'mvn org.pitest:pitest-maven:mutationCoverage'
                publishHTML(target: [
                        reportDir  : 'target/pit-reports',
                        reportFiles: 'index.html',
                        reportName : 'Mutation Tests (PIT)'
                ])
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh "mvn verify -DskipUTs -DskipITs org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=lmsbooks -Dsonar.projectName='lmsbooks' -Dsonar.host.url='http://lms-isep.ovh:9000' -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        stage('Manual Approval') {
            steps {
                slackSend(
                    color: '#f0544c',
                    message: "Deploy manual approve needed (<${env.BUILD_URL}|Check Console>)"
                )
                input message: 'Approve deployment?', ok: 'Go On'
            }
        }

        stage('Coverage') {
            steps {
                publishHTML(target: [
                        reportDir  : 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName : 'JaCoCo Coverage'
                ])
            }
        }

        stage('Pact Contract Tests') {
            steps {
                sh 'mvn test -Ppact-provider'
                publishHTML(target: [
                        reportDir  : 'target/pacts',
                        reportFiles: '*.html',
                        reportName : 'Pact Contract Tests'
                ])
            }
        }

        stage('Deploy @ Staging') {
            when {
                expression {
                    return env.GIT_BRANCH == 'origin/staging' || env.GIT_BRANCH == 'staging'
                }
            }
            steps {
                sh 'chmod 600 ./deployment-resources/id_rsa_custom'
                sh 'scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/LMSBooks-*.jar staging:/opt/books/staging/LMSBooks.jar'
            }
        }

        stage('Deploy @ Production') {
            when {
                expression {
                    return env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main'
                }
            }
            steps {
                sh 'chmod 600 ./deployment-resources/id_rsa_custom'
                sh 'scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/LMSBooks-*.jar production:/opt/books/main/LMSBooks.jar'
            }
        }

        stage('k6 Production Load Tests') {
            when {
                expression {
                    return env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main'
                }
            }
            steps {
                sh 'k6 run load-tests/smoke/get-books-smoke.js'
                sh 'k6 run load-tests/smoke/create-book-smoke.js'
            }
        }
    }

    post {
        always {
            slackSend(
                color: "${currentBuild.currentResult == 'SUCCESS' ? 'good' : 'danger'}",
                message: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER}: ${currentBuild.currentResult} (<${env.BUILD_URL}|Open Jenkins>)"
            )
        }
    }

}