def call() {
    withSonarQubeEnv('Sonar Valcome') {
        def scannerHome = tool 'SonarScanner 4.8';
        sh "${scannerHome}/bin/sonar-scanner"
    }
}
