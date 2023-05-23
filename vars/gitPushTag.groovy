def call(version, key) {
    sshagent(credentials: ["${key}"]) {
        checkout scm
        sh 'git config --global user.email "dev@valcome.at"'
        sh 'git config --global user.name "valcome-analytics"'
        sh "git tag ${version} || true"
        sh "git push origin ${version}"
    }
}
