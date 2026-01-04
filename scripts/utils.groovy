// At the end of the file we must return 'this'

def sendNotification(color, message) {
    slackSend(
            color: "${color}",
            message: "${message}"
    )
}

def runLoadTest(scriptPath, reportName) {
    sh(script: "k6 run -e ${scriptPath}" )
    publishReport(path: '.', file: 'summary.html', name: reportName)
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