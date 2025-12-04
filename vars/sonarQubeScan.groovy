def call(version = '') {
    withSonarQubeEnv('Sonar Valcome') {
        def scannerHome = tool 'SonarScanner 8';

         if ("${version}" != '') {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectVersion=${version}"
        } else {
            sh "${scannerHome}/bin/sonar-scanner"
        }
    }
}
