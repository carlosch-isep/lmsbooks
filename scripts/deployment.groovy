// Importante: No final deste ficheiro tens de retornar 'this'
def deploy(branch, strategy) {

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

    if(strategy == 'Switch'){
        // --- INIT SWITCH VERSION
        sh "${ssh} ${branch} 'docker network inspect lms_network >/dev/null 2>&1 || docker network create lms_network'"
        sh "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/LMSBooks-0.0.1-SNAPSHOT.jar ${branch}:/opt/books/${branch}/target/LMSBooks-0.0.1-SNAPSHOT.jar"
        sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && docker rm -f books_query books_command'"
        sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && IMAGE_TAG=${imageTag} docker compose pull && docker compose up -d'"
    } else {
        // --- INIT CANARY VERSION

        // Make sure swarm is active
        sh """${ssh} ${branch} '
        if ! docker info | grep -q "Swarm: active"; then
            docker swarm init
        fi
        '
        """

        // Make sure network exists
        """ ${ssh} ${branch} '
        if ! docker network ls | grep -q "lms_network"; then
            docker network create --driver overlay --attachable lms_network
        fi
        '
        """

        sh "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/LMSBooks-0.0.1-SNAPSHOT.jar ${branch}:/opt/books/${branch}/target/LMSBooks-0.0.1-SNAPSHOT.jar"

        // Proxy
        sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && TRAEFIK_PORT=8070 docker stack deploy -c docker-compose-traefik.yml proxy'"

        // Stack
        sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && IMAGE_TAG=${imageTag} STABLE_TAG=${imageTag} docker stack deploy -c docker-compose-swarm.yml ${branch} --with-registry-auth'"
    }
}

def dockerConfig(branch){
    // Set permissions
    sh 'chmod 600 ./deployment-resources/id_rsa_custom'
    // Copy rollout files
    sh "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config -r ./rollout ${branch}:/opt/books/${branch}"
    // Copy docker files
    sh "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config *ocker* ${branch}:/opt/books/${branch}"
}

return this