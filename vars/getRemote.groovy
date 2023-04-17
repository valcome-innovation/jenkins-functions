static def call(host,
                user,
                passphrase,
                identity,
                allowAnyHosts) {
    return [
            name         : "SSH Target",
            host         : host,
            user         : user,
            passphrase   : passphrase,
            identityFile : identity,
            allowAnyHosts: allowAnyHosts,
    ]
}