def call(remote, directory, branch) {
    sshCommand remote: remote,
            command: "cd ${directory} && git fetch --all && git reset --hard origin/${branch}"
}