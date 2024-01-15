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
        def sshPassphase = null

        try {
            sshPassphase = passphrase
        } catch (MissingPropertyException e) {
        }

        def REMOTE = getRemote(HOST, user, sshPassphase, identity, true)

        closure.delegate = [(remoteName): REMOTE]
        closure()
    }
}
