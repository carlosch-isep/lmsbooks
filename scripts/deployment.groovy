// Importante: No final deste ficheiro tens de retornar 'this'
def deploy(branch) {

    def imageTag = ""

    if (params.ROLLBACK_TAG?.trim()) {
        imageTag = params.ROLLBACK_TAG
    } else {
        imageTag = "${branch}-${env.BUILD_NUMBER}"
    }

    def config = [
            'Staging':'staging',
            'Production':'production'
    ]

    // Set permissions
    sh 'chmod 600 ./deployment-resources/id_rsa_custom'

    // SSH init configs
    def ssh = "ssh -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config"

    // Rollback to tag:
    sh "${ssh} ${config[branch]} 'cd /opt/books/${config[branch]}/ && IMAGE_TAG=${imageTag} docker compose pull && docker compose up -d'"
}

return this