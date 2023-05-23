def call() {
    timeout(time: 1, unit: 'HOURS') {
        def qg = waitForQualityGate(webhookSecretId: 'SONAR_WEBHOOK_SECRET')
        def status = 'SUCCESS'

        if (qg.status != 'OK') {
            status = 'FAILURE'
        }

        return status
    }
}
