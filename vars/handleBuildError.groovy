def call(exc) {
    currentBuild.result = 'FAILURE'

    sendGoogleChatMessage(exc)
}