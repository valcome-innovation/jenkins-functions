package org.valcome

class AWSS3Deployment implements Serializable {

    def steps
    def params

    AWSS3Deployment(steps = null,
                    image = null,
                    environment = null,
                    version = null
    ) {
        this.steps = steps
        this.params = [
            image: image,
            env: environment,
            version: version
        ]
    }

    def deploy(String srcBucket,
               String destBucket) {
        if (!checkVersionExists(srcBucket)) {
            steps.error "Version doesn't exist at s3://${getSourceS3Path(srcBucket)}/"
        }

        cleanDeploymentBucket(destBucket)
        copyNewVersion(srcBucket, destBucket)
    }

    def checkVersionExists(String srcBucket) {
        def s3URI = getSourceS3Path(srcBucket)
        def listCommand = "aws s3 ls s3://${s3URI}/"

        try {
            def result = steps.sh script: "${listCommand}", returnStdout: true
            return result
        } catch (e) {
            return false
        }
    }

    String getSourceS3Path(String srcBucket) {
        return "${srcBucket}/artifacts/${params.image}/${params.env}/${params.version}"
    }

    def copyNewVersion(String srcBucket,
                       String destBucket) {
        def deployCommand = """
        aws s3 cp \
        s3://${getSourceS3Path(srcBucket)} \
        s3://${destBucket}/ \
        --recursive
        """

        steps.sh script: "${deployCommand}", returnStdout: true
    }

    private def cleanDeploymentBucket(bucket) {
        def cleanCommand = "aws s3 rm s3://${bucket}/ --recursive"

        steps.sh script: "${cleanCommand}", returnStdout: true
    }

    def deployEnvFile(String envFilePath,
                      String destBucket) {
        def command = """
        aws s3 cp \
        ${envFilePath}\
        s3://${destBucket}/${envFilePath}
        """

        steps.sh script: "${command}", returnStdout: true
    }

    def invalidateCloudfrontCaches(cloudfrontId) {
        def invalidateCommand = """
        aws cloudfront create-invalidation \
        --distribution-id ${cloudfrontId} \
        --paths '/*' \
        """

        steps.sh script: "${invalidateCommand}", returnStdout: true

        steps.sleep 15
    }
}