def call(exc) {
    currentBuild.result = 'FAILURE'

    notifyGoogleChat(exc)
}