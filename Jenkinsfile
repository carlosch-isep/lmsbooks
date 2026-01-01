pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
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
                sh 'mvn package -DskipTests'
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
                withSonarQubeEnv('MySonarQubeServer') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate and Coverage') {
            parallel {
                stage('Quality Gate') {
                    steps {
                        timeout(time: 5, unit: 'MINUTES') {
                            script {
                                def qg = waitForQualityGate()
                                if (qg.status != 'OK') {
                                    error "Pipeline aborted due to quality gate failure: ${qg.status}"
                                }
                            }
                        }
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

        stage('k6 Load Tests') {
            steps {
                sh 'k6 run load-tests/smoke/get-books-smoke.js'
                sh 'k6 run load-tests/smoke/create-book-smoke.js'
            }
        }

        stage('Manual Approval') {
            steps {
                input message: 'Approve deployment?', ok: 'Go On'
            }
        }

        stage('Deploy @ Staging') {
            when {
                branch 'staging'
            }
            steps {
                sh 'chmod 600 ./deployment-resources/id_rsa_custom'
                sh 'scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/psoft-g1-0.0.1-SNAPSHOT.war staging:/opt/tomcat/webapps/psoft-g1-0.0.1-SNAPSHOT.war'
            }
        }

        stage('Deploy @ Production') {
            when {
                branch 'main'
            }
            steps {
                sh 'chmod 600 ./deployment-resources/id_rsa_custom'
                sh 'scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/psoft-g1-0.0.1-SNAPSHOT.war production:/opt/tomcat/webapps/psoft-g1-0.0.1-SNAPSHOT.war'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed!'
        }
    }

}