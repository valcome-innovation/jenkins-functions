def call(remote, env, value) {
    writeFile file: env,
            text: value

    sshPut remote: remote,
            from: env,
            into: env
}