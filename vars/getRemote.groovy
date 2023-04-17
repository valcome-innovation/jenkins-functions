static def call(host,
                user,
                passphrase,
                identity,
                allowAnyHosts) {
    def sshTarget = [
            name         : "SSH Target",
            host         : host,
            user         : user,
            identityFile : identity,
            allowAnyHosts: allowAnyHosts,
    ]

    if (passphrase != null) {
        sshTarget.passphrase = passphrase
    }

    return sshTarget
}