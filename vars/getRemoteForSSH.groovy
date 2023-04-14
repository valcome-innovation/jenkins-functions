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
        return getRemote(
                host,
                user,
                passphrase,
                identity,
                allowAnyHosts
        )
    }
}