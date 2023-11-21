def call(msg, envs = '') {
    def message = """
    *${env.JOB_BASE_NAME} #${env.BUILD_NUMBER}* _${currentBuild.currentResult} after ${currentBuild.durationString.replace(' and counting', '')}_
    ${env.BUILD_URL}
    ${msg}
    ${envs}
    """

    sendGoogleChatMessage(message)
}