def call(remote, directory, tag) {
    sshCommand remote: remote,
            command: "cd ${directory} && git tag ${tag} && git push origin ${tag}"
}
