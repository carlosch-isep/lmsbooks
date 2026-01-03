// At the end of the file we must return 'this'

def sendNotification(color, message) {
    slackSend(
            color: "${color}",
            message: "${message}"
    )
}

def runLoadTest(scriptPath, reportName) {
    try {
        sh "k6 run -e ${scriptPath}"
    } catch (Exception e) {
        echo "⚠️ O k6 falhou (talvez um threshold?), mas vamos publicar o relatório na mesma.: ${e}"
    } finally {
        publishReport(path: '.', file: 'summary.html', name: reportName)
    }
}

def publishReport(Map config = [:]) {
    // allow permanet link to last build
    archiveArtifacts artifacts: "${config.path}/**/*", allowEmptyArchive: true

    publishHTML(target: [
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: config.path,
            reportFiles: config.file,
            reportName: config.name
    ])
}

return this