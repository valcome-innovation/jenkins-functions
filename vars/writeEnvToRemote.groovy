def call(remote,
         env,
         value,
         target=env) {
    writeFile file: env,
            text: value

    sshPut remote: remote,
            from: env,
            into: env
}