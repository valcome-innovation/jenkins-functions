def call(remote, secret) {
    withCredentials([string(credentialsId: secret, variable: 'SECRET_VALUE')]) {
        write_env_to_remote(
                remote,
                secret,
                SECRET_VALUE
        )
    }
}
