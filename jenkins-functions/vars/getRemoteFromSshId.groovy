static def call(sshId,
                host,
                allowAnyHosts) {
    withCredentials([
            sshUserPrivateKey(
                    credentialsId: "${sshId}",
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