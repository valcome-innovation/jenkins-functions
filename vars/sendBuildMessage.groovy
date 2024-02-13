def call(msg, envs = '') {
    def message = """*${env.JOB_BASE_NAME} <${env.BUILD_URL}|#${env.BUILD_NUMBER}>* - ${currentBuild.currentResult} after ${currentBuild.durationString.replace(' and counting', '')}
${msg}${envs ? "\n" : ""}${envs}
"""

    sendGoogleChatMessage(message)
}