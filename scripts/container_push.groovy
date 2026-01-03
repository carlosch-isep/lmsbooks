// Remember return 'this'
def containerPush(imageTag, dockerUser, dockerPass) {
    sh """
      echo "${dockerPass}" | docker login -u "${dockerUser}" --password-stdin
      docker build --platform=linux/amd64 -t nunoaraujo12/demo-psoft:${imageTag} .
      docker push nunoaraujo12/demo-psoft:${imageTag}
    """
}

return this