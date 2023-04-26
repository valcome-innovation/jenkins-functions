def call(exc) {
    currentBuild.result = 'FAILURE'

    sendBuildMessage(exc)
}