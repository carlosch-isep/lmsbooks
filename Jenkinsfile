def utils
def containerPush
def deployment
def release

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                script {
                    utils = load 'scripts/utils.groovy'
                    containerPush = load 'scripts/container_push.groovy'
                    deployment = load 'scripts/deployment.groovy'
                    release = load 'scripts/release.groovy'
                }
            }
        }

        stage('Initialize') {
            steps {
                script {
                    utils.sendNotification('#f0544c',
                    "🚀 *Started:* Job ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}] (<${env.BUILD_URL}|Check Console>)")
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                sh 'mvn clean'
            }
        }

        stage('Build & Tests') {
            steps {
                sh 'mvn package -DskipTests'
                sh 'mvn test -DskipITs'
                sh 'mvn verify -DskipUTs'
            }
        }

        stage('Mutation tests') {
            steps {
                script {
                    try {
                        sh 'mvn org.pitest:pitest-maven:mutationCoverage'
                    } finally {
                        utils.publishReport(
                            path: 'target/pit-reports',
                            file: 'index.html',
                            name: "Mutation Tests (PIT)"
                        )
                    }
                }
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
                script {
                    utils.sendNotification('#f0544c', "⚠️ Deploy manual approval needed (<${env.BUILD_URL}|Check Console>)")
                }
                input message: 'Approve deployment?', ok: 'Go On'
            }
        }

        stage('Post-Build Reports') {
            steps {
                script {
                    utils.publishReport(path: 'target/site/jacoco', file: 'index.html', name: "JaCoCo Coverage")
                    sh 'mvn test -Ppact-provider'
                    utils.publishReport(path: 'target/pacts', file: '*.html', name: "Pact Contract Tests")
                }
            }
        }

        stage('CONTAINER IMAGE BUILD & PUSH') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    script {
                        containerPush.containerPush("${env.BRANCH_NAME}-${env.BUILD_NUMBER}", DOCKER_USER, DOCKER_PASS)
                    }
                }
            }
        }

        stage("Deploy") {
            steps {
                script {
                    deployment.deploy(env.BRANCH_NAME)
                }
            }
        }

        stage('k6 Production Load Tests') {
            steps {
                script {
                    utils.runLoadTest("load-tests/smoke/get-books-smoke.js", 'K6 Smoke Get Books Report')
                    utils.runLoadTest("load-tests/smoke/create-book-smoke.js", 'K6 Smoke Post Books Report')
                }
            }
        }
    }

    post {
        success {
            script { utils.sendNotification('#f0544c', "Sucesso total! 🚀") }
        }
        failure {
            script { utils.sendNotification('danger', "Algo correu mal... ❌") }
        }
    }
}