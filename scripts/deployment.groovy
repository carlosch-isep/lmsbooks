// Importante: No final deste ficheiro tens de retornar 'this'
def deploy(branch, strategy) {

    def imageTag = ""

    if (params.ROLLBACK_TAG?.trim()) {
        imageTag = params.ROLLBACK_TAG
    } else {
        imageTag = "${branch}-${env.BUILD_NUMBER}"
    }

    def config = [
        dev: ["CADDY_HTTP_PORT": "18080", "CADDY_HTTPS_PORT": "18443", "STABLE_TAG" : imageTag, "IMAGE_TAG": imageTag],
        staging:  ["CADDY_HTTP_PORT": "8080", "CADDY_HTTPS_PORT": "8443", "STABLE_TAG" : imageTag, "IMAGE_TAG": imageTag],
        production: ["CADDY_HTTP_PORT": "80", "CADDY_HTTPS_PORT": "443", "STABLE_TAG" : imageTag, "IMAGE_TAG": imageTag]
    ]

    // Set permissions
    sh 'chmod 600 ./deployment-resources/id_rsa_custom'

    // SSH init configs
    def ssh = "ssh -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config"
    def scp = "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config"

    // Send jar
    sh "${scp} target/LMSBooks-0.0.1-SNAPSHOT.jar ${branch}:/opt/books/${branch}/target/LMSBooks-0.0.1-SNAPSHOT.jar"

    if(strategy == 'Switch'){
        // --- INIT SWITCH VERSION
        sh "${ssh} ${branch} 'docker network inspect lms_network >/dev/null 2>&1 || docker network create lms_network'"
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
        sh """${ssh} ${branch} '
        NETWORK_DRIVER=\$(docker network inspect lms_network --format "{{.Driver}}" 2>/dev/null || echo "missing")
        
        if [ "\$NETWORK_DRIVER" = "bridge" ]; then
            docker network rm lms_network || true
            NETWORK_DRIVER="missing"
        fi

        if [ "\$NETWORK_DRIVER" != "overlay" ]; then
            docker network create --driver overlay --attachable lms_network
        fi
        '
        """
        // Remove previous stack
        sh "${ssh} ${branch} 'docker stack rm ${branch}'"

        // Var env
        def varEnv = config[branch].collect { k, v -> " ${k}=${v}" }.join(" ")

        // Stack
        sh "${ssh} ${branch} 'cd /opt/books/${branch}/ && ${varEnv} docker stack deploy -c docker-compose-swarm.yml ${branch} --with-registry-auth'"
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