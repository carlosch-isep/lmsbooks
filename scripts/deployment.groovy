// Importante: No final deste ficheiro tens de retornar 'this'
def deploy(branch) {

    def imageTag = ""

    if (params.ROLLBACK_TAG?.trim()) {
        imageTag = params.ROLLBACK_TAG
    } else {
        imageTag = "${branch}-${env.BUILD_NUMBER}"
    }

    // Set permissions
    sh 'chmod 600 ./deployment-resources/id_rsa_custom'

    // SSH init configs
    def ssh = "ssh -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config"

    // Rollback to tag:
    sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && IMAGE_TAG=${imageTag} docker compose pull && docker compose up -d'"
}

def dockerConfig(branch){
    sh "scp -o StrictHostKeyChecking=no -i ./deployment-resources/ssh_deployment_config *ocker* ${branch}:/opt/books/${branch}"
}

return this