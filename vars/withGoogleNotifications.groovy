def call(envs = '', closure) {
    try {
        closure()

        sendBuildMessage('', envs)
    } catch (exc) {
        handleBuildError("💥 " + exc.toString())
    }
}