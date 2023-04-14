def call(sshSecretId,
         host,
         allowAnyHosts) {
    def remote

    withCredentials([
            sshUserPrivateKey(
                    credentialsId: "${sshSecretId}",
                    keyFileVariable: 'identity',
                    passphraseVariable: 'passphrase',
                    usernameVariable: 'user'
            )
    ]) {
        remote = getRemote(
                host,
                user,
                passphrase,
                identity,
                allowAnyHosts
        )
    }

    return remote
}