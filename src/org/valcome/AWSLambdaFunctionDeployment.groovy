package org.valcome

class AWSLambdaFunctionDeployment implements Serializable {
    def steps
    String function
    String codeFilePath

    AWSLambdaFunctionDeployment(steps = null,
                                function = null,
                                codeFilePath = null) {
        this.steps = steps
        this.function = function
        this.codeFilePath = codeFilePath
    }

    public def deployWithTests(closure) {
        updateFunction()

        closure()

        publishVersion()
    }

    public def updateFunction() {
        steps.zip zipFile: "./code.zip", \
            dir: "${codeFilePath}", \
            overwrite: true, \
            glob: "*.mjs"

        steps.sh script: """
        aws lambda update-function-code \
        --function-name ${function} \
        --zip-file fileb://code.zip
        """
    }

    public def runTestEvent(String testEventFilePath) {
        String jsonTextOutput = steps.sh script: """
        aws lambda invoke \
        --function-name ${function} \
        --payload file://${testEventFilePath} \
        --cli-binary-format raw-in-base64-out \
        --output json \
        test-output.json
        """, returnStdout: true

        steps.sh "cat test-output.json"
        steps.sh "echo ${jsonTextOutput}"

        def jsonTestOutput = steps.readJSON file: 'test-output.json'
        return jsonTestOutput
    }

    public def publishVersion() {
        steps.sh script: """
        aws lambda publish-version \
        --function-name ${function} \
        """
    }
}