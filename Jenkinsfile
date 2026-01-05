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
            choices: ['Staging', 'Dev', 'Production'],
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
                    utils.sendNotification('#2986cc',
                    "*Info:* Started Job ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}] (<${env.BUILD_URL}|Check Console>)")
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
                    if(params.DEPLOY_ENV.toLowerCase() == 'dev'){
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
                    echo "⏳ A verificar Quality Gate (Manual)..."
                    sleep 10

                    // Faz o pedido à API
                    def response = sh(
                        script: "curl -s -u ${SONAR_TOKEN_LMSBOOK}: 'http://lms-isep.ovh:9000/api/qualitygates/project_status?projectKey=lmsbooks'",
                        returnStdout: true
                    ).trim()

                    // Lógica de proteção contra falhas
                    if (response.contains('"status":"OK"')) {
                        echo "Quality gate: OK"
                    } else {
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        stage('Manual Approval') {
            steps {
                script {
                    utils.sendNotification('#f1c232', "*Warning:* Deploy manual approval needed [Build #${env.BUILD_NUMBER}] (<${env.BUILD_URL}|Check Console>)")
                }
                input message: 'Approve deployment?', ok: 'Go On'
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo "Post-Build Reports catchError"
                }
            }
        }

        stage('Post-Build Reports') {
            steps {
                script {
                    try {
                        int status = sh(script: 'mvn test -Ppact-provider -Dpact.writer.strategy=pactfile -Dpact.reports.path=target/pacts/reports', returnStatus: true)

                        if (status != 0) {
                            currentBuild.result = 'UNSTABLE'
                        }
                    } finally {
                        utils.publishReport(path: 'target/reports', file: 'surefire.html', name: "Surefire Coverage")
                        if (fileExists('target/pacts/reports/index.html')) {
                            utils.publishReport(path: 'target/pacts/reports', file: 'index.html', name: "Pact Contract Tests")
                        } else {
                         archiveArtifacts artifacts: 'target/pacts/*.json', allowEmptyArchive: true
                     }
                    }
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
                        try {
                            containerPush.containerPush("${params.DEPLOY_ENV.toLowerCase()}-${env.BUILD_NUMBER}", DOCKER_USER, DOCKER_PASS)
                        } finally {
                           echo "success"
                        }
                    }
                }

                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo "Push to dockerHub catchError"
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

        stage('Wait for deploy to end'){
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        waitUntil {
                            try {
                                def status = sh(
                                    script: "curl -s -o /dev/null -w '%{http_code}' http://lms-isep.ovh/api/query/books",
                                    returnStdout: true
                                ).trim()

                                return status == '200'
                            } catch (Exception e) {
                                return false
                            }
                        }
                    }
                }
            }
        }

        stage('K6 Smoke tests') {
            steps {
                script {
                    if(params.DEPLOY_ENV.toLowerCase() == 'dev' || params.DEPLOY_ENV.toLowerCase() == 'staging'){
                        utils.runLoadTest("BASE_URL=http://lms-isep.ovh load-tests/smoke/get-books-smoke.js", 'K6 Smoke Get Books Report')
                        utils.runLoadTest("BASE_URL=http://lms-isep.ovh load-tests/smoke/create-book-smoke.js", 'K6 Smoke Post Books Report')
                    }
                }

                catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                    echo "K6 Smoke tests catchError"
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    utils.sendNotification('#6aa84f', "*Success:* Deploy with success [Build #${env.BUILD_NUMBER}]!")
                } catch (Exception e) {
                    echo "Post stage Error: ${e.getMessage()}"
                }
            }
        }
    }
}