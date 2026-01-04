// At the end of the file we must return 'this'

def sendNotification(color, message) {
    slackSend(
            color: "${color}",
            message: "${message}"
    )
}

def runLoadTest(scriptPath, reportName) {
    int exitCode = sh(script: "k6 run -e ${scriptPath}", returnStatus: true )

    publishReport(path: '.', file: 'summary.html', name: reportName)

    if (exitCode == 99) {
        echo "⚠️ Thresholds do k6 não foram atingidos (Exit Code 99)."
        // Define o build como Amarelo no Jenkins
        currentBuild.result = 'UNSTABLE'
    } else if (exitCode != 0) {
        // Se for qualquer outro erro (1, 100, etc), o pipeline deve parar mesmo
        error "❌ O k6 terminou com um erro crítico (Código: ${exitCode})."
    } else {
        echo "✅ Testes de carga passaram em todos os thresholds!"
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