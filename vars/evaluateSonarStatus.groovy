def call(String projectName) {
    withCredentials([
        string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')
    ]) {
        def authHeader = 'Authorization: Bearer $SONAR_TOKEN'
        def curl = """curl -sS -H "$authHeader" 'https://sonar.valcome.dev/api/qualitygates/project_status?projectKey=$projectName'"""
        String response = sh script: "$curl",
                returnStdout: true

        def responseJSON = readJSON text: response
        def sonarStatus = responseJSON.projectStatus.status
        echo "SONAR analysis status is: $sonarStatus"

        if (responseJSON.projectStatus.status != "ERROR") {
            def detailsUrl = "https://sonar.valcome.dev/dashboard?id=$projectName"
            publishGithubCheck(
                    "Sonar",
                    "Analysis status: $sonarStatus",
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
