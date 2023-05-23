def call(version = null) {
    withSonarQubeEnv('Sonar Valcome') {
        def scannerHome = tool 'SonarScanner 4.8';

         if ("${version}" != null) {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectVersion=${version}"
        } else {
            sh "${scannerHome}/bin/sonar-scanner"
        }
    }
}
