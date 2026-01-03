// Remember return 'this'
def containerPush(imageTag, dockerUser, dockerPass) {
    sh """
      echo "${dockerPass}" | docker login -u "${dockerUser}" --password-stdin
      docker build --platform=linux/amd64 -t carloshenriquesisep/lmsbooks:${imageTag} .
      docker push carloshenriquesisep/lmsbooks:${imageTag}
    """
}

return this