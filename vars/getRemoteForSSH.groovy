def call(sshSecretId,
         host,
         allowAnyHosts) {
    withCredentials([
            sshUserPrivateKey(
                    credentialsId: "${sshSecretId}",
                    keyFileVariable: 'identity',
                    passphraseVariable: 'passphrase',
                    usernameVariable: 'user'
            )
    ]) {
        return get_remote(
                host,
                user,
                passphrase,
                identity,
                allowAnyHosts
        )
    }
}