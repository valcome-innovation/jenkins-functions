def call(testEnv, branch, host) {
    withSshRemote('ARES_SSH', host, 'REMOTE') {

        gitForcePull(REMOTE, '~/git/ng-live-suite/ ', branch)

        sshCommand remote: REMOTE,
            command: """
            cd ~/git/ng-live-suite/ && \
            yarn install --production=false && \
            yarn test:e2e:${testEnv}
            """
    }
}
