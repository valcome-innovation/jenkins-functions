def call(msg, envs = '') {
    def message = """
    *${env.JOB_BASE_NAME} #${env.BUILD_NUMBER}*
    ${env.BUILD_URL}
    _finished in ${currentBuild.durationString.replace(' and counting', '')}_
    ${currentBuild.currentResult}
    ${msg}

    ${envs}
    """

    sendGoogleChatMessage(message)
}