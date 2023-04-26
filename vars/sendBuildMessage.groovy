def call(msg, envs) {
    def message = """
        *${env.JOB_BASE_NAME} #${env.BUILD_NUMBER}*
        ${currentBuild.currentResult}
        ${msg}
        
        _finished in ${currentBuild.durationString.replace(' and counting', '')}_

        ${envs}
    """

    notifyGoogleChat(message)
}