def call(remote, secret) {
    withCredentials([string(credentialsId: secret, variable: 'SECRET_VALUE')]) {
        writeEnvToRemote(
                remote,
                secret,
                SECRET_VALUE
        )
    }
}
