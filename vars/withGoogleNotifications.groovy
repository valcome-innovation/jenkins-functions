

def call(envs = '', closure) {
    try {
        closure().call()

        sendBuildMessage('', envs)
    } catch (exc) {
        handleBuildError(exc)
    }
}