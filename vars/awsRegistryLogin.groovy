def call(awsAccessKeyIdSecret,
         awsSecretKeySecret,
         registry) {

    withCredentials([
            string(credentialsId: awsAccessKeyIdSecret, variable: 'AWS_ACCESS_KEY_ID'),
            string(credentialsId: awsSecretKeySecret, variable: 'AWS_SECRET_ACCESS_KEY')
    ]) {
        sh "aws --version"
        sh 'aws configure set aws_access_key_id "$AWS_ACCESS_KEY_ID"'
        sh 'aws configure set aws_secret_access_key "$AWS_SECRET_ACCESS_KEY"'
        sh "aws configure set default.region eu-central-1"
        sh "aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin ${registry}"
    }
}