def call(callback, envs) {
    try {
        callback()

        sendBuildMessage('', envs)
    } catch (exc) {
        handleBuildError(exc)
    }
}