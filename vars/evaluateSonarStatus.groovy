def call(String projectName) {
    withCredentials([
        string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')
    ]) {
        def authHeader = 'Authorization: Bearer $SONAR_TOKEN'
        def curl = "curl -sS -H $authHeader https://sonar.valcome.dev/api/qualitygates/project_status?projectKey=$projectName"
        String response = sh script: "$curl",
                returnStdout: true
        sh "echo $response"

        def responseJSON = readJSON text: response

        if (responseJSON.projectStatus.status == "ERROR") {
            def detailsUrl = "https://sonar.valcome.dev/dashboard?id=${projectName}"
            publishGithubCheck(
                    "Sonar",
                    "Sonar",
                    "COMPLETED",
                    "NEUTRAL",
                    "SONAR analysis resulted in ERROR state. Please check the details",
                    "",
                    detailsUrl
            )
        }

        return responseJSON.projectStatus.status
    }
}
