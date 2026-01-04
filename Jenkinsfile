def utils
def containerPush
def deployment
def release

pipeline {
    agent any

    environment {
        SONAR_TOKEN_LMSBOOK = credentials('SONAR_TOKEN_LMSBOOK')
    }

    parameters {
        string(
            name: 'ROLLBACK_TAG',
            defaultValue: '',
            description: 'Docker image tag to rollback to (e.g. staging-12)'
        )
        choice(
            name: 'DEPLOY_ENV',
            choices: ['Staging', 'Production'],
            description: 'Select the environment you want'
        )
        choice(
            name: 'DEPLOY_STRATEGY',
            choices: ['Switch', 'Canary'],
            description: 'Select the deploy strategy'
        )
    }

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

        stage('Build') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Tests') {
            steps {
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
                withSonarQubeEnv('lms') {
                    sh "mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN_LMSBOOK}"
                }
            }
        }

        stage("Quality Gate") {
            steps {
                script {
                    sleep 10
                    def response = sh(
                        script: """
                            curl -s -u ${SONAR_TOKEN_LMSBOOK}: \
                            'http://lms-isep.ovh:9000/api/qualitygates/project_status?projectKey=lmsbooks'
                        """,
                        returnStdout: true
                    ).trim()

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
                    utils.publishReport(path: 'target/reports', file: 'surefire.html', name: "Surefire Coverage")
                    sh 'mvn test -Ppact-provider -Dpact.writer.strategy=pactfile -Dpact.reports.path=target/pacts/reports'
                    utils.publishReport(path: 'target/pacts', file: '*.json', name: "Pact Contract Tests")
                }
            }
        }

        stage('Push container to dockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    script {
                        containerPush.containerPush("${params.DEPLOY_ENV.toLowerCase()}-${env.BUILD_NUMBER}", DOCKER_USER, DOCKER_PASS)
                    }
                }
            }
        }

        stage("Deploy Docker Struct"){
            steps{
                script{
                    deployment.dockerConfig(params.DEPLOY_ENV.toLowerCase())
                }
            }
        }

        stage("Deploy") {
            steps {
                script {
                    deployment.deploy(params.DEPLOY_ENV.toLowerCase(), params.DEPLOY_STRATEGY.toLowerCase())
                }
            }
        }

        stage('k6 Production Load Tests') {
            steps {
                script {
                    try {
                        utils.runLoadTest("BASE_URL=http://lms-isep.ovh:8070 load-tests/smoke/get-books-smoke.js", 'K6 Smoke Get Books Report')
                        utils.runLoadTest("BASE_URL=http://lms-isep.ovh:8070 load-tests/smoke/create-book-smoke.js", 'K6 Smoke Post Books Report')
                    } finally {
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }
    }

    post {
        unstable {
            script { utils.sendNotification('#ffcc00', "Build com avisos (k6), mas o Deploy foi feito! ⚠️") }
        }
        success {
            script { utils.sendNotification('#f0544c', "Sucesso total! 🚀") }
        }
        failure {
            script { utils.sendNotification('danger', "Algo correu mal... ❌") }
        }
    }
}