// Importante: No final deste ficheiro tens de retornar 'this'
def deploy(branch) {

    def config = [
            'staging':'staging',
            'origin/staging':'staging',
            'main':'production',
            'origin/main':'production'
    ]
    sh 'chmod 600 ./deployment-resources/id_rsa_custom'
    sh "scp -o StrictHostKeyChecking=no -F ./deployment-resources/ssh_deployment_config target/LMSBooks-*.jar ${config[branch]}:/opt/books/staging/LMSBooks.jar"
}

return this