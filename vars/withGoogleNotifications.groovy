def call(closure) {
    try {
        closure().call()

        sendBuildMessage('', '')
    } catch (exc) {
        handleBuildError(exc)
    }
}