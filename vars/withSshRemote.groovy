def call(SSH_ID,
         HOST,
         remoteName = 'REMOTE',
         closure) {
    withCredentials([
            sshUserPrivateKey(
                    credentialsId: "${SSH_ID}",
                    keyFileVariable: 'identity',
                    passphraseVariable: 'passphrase',
                    usernameVariable: 'user'
            )
    ]) {
        if (env.SSH_PASSPHRASE == null) {
                echo "SSH Key without passphrase"
        }

        def REMOTE = getRemote(HOST, user, passphrase, identity, true)

        closure.delegate = [(remoteName): REMOTE]
        closure()
    }
}
