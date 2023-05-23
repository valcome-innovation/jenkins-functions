def call() {
    withSonarQubeEnv('Sonar Valcome') {
        publishGithubCheck("Quality Gate", "Running Analysis", "IN_PROGRESS")

        def scannerHome = tool 'SonarScanner 4.8';
        sh "${scannerHome}/bin/sonar-scanner"
    }

    timeout(time: 1, unit: 'HOURS') {
        publishGithubCheck("Quality Gate", "Waiting for Analysis result", "IN_PROGRESS")

        def qg = waitForQualityGate(webhookSecretId: 'SONAR_WEBHOOK_SECRET')
        def status = 'SUCCESS'

        if (qg.status != 'OK') {
            status = 'FAILURE'
        }

        publishGithubCheck("Quality Gate", "Analysis finished ${qg.status}", "COMPLETED", status)
    }
}
