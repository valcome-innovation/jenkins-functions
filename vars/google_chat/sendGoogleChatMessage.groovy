package google_chat

def call(message) {
    withCredentials([string(credentialsId: 'GOOGLE_CHAT_JENKINS_KEY', variable: 'SECRET_VALUE')]) {
        sh '''
        curl \
        -H "Content-Type: application/json; charset=UTF-8" \
        --data '{"text": \"''' + message + '''\"'} \
        -X POST "https://chat.googleapis.com/v1/spaces/AAAA5SJWKzg/messages?key=$SECRET_VALUE"
        '''
    }
}