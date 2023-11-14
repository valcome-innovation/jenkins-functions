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

    def deploy(srcBucket, destBucket) {
        def deployCommand = """
        aws s3 cp \
        s3://${srcBucket}/artifacts/${params.image}/${params.env}/${params.version} \
        s3://${destBucket}/ \
        --recursive
        """

        steps.sh script: "${deployCommand}", returnStdout: true
    }

    def invalidateCloudfrontCaches(cloudfrontId) {
        def invalidateCommand = """
        aws cloudfront create-invalidation \
        --distribution-id ${cloudfrontId} \
        --paths /
        """

        steps.sh script: "${invalidateCommand}", returnStdout: true

        steps.sleep 15
    }
}