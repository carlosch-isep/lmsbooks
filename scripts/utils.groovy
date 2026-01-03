// At the end of the file we must return 'this'

def sendNotification(color, message) {
    slackSend(
            color: "${color}",
            message: "${message}"
    )
}

def runLoadTest(scriptPath, reportName) {
    echo "Running k6: ${scriptPath}"

    // Execute k6
    sh "k6 run ${scriptPath}"

    // Publish the generated HTML
    publishReport(target: [
            allowAntFiles: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '.',            // Where summary.html is located
            reportFiles: 'summary.html',
            reportName: "${reportName}"
    ])
}

def publishReport(Map config = [:]) {
    // allow permanet link to last build
    archiveArtifacts artifacts: "${config.path}/**/*", allowEmptyArchive: true

    publishHTML(target: [
            allowAntFiles: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: config.path,
            reportFiles: config.file,
            reportName: config.name
    ])
}

return this