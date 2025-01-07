package org.valcome

class AWSCloudFrontFunctionDeployment implements Serializable {
    def steps
    String function
    String codeFilePath

    AWSCloudFrontFunctionDeployment(steps = null,
                                    function = null,
                                    codeFilePath = null) {
        this.steps = steps
        this.function = function
        this.codeFilePath = codeFilePath
    }

    public def deployWithTests(closure) {
        uploadFunction()

        closure()

        publishFunction()
    }

    public def uploadFunction() {
        def functionJSON = describeFunction()
        String etag = getETag(functionJSON)
        def functionConfig = getFunctionConfig(functionJSON)

        steps.sh script: """
        aws cloudfront update-function \
        --name ${function} \
        --if-match ${etag} \
        --function-code fileb://${codeFilePath} \
        --function-config '${functionConfig}'
        """
    }

    public def runTestEvent(String testEventFilePath,
                            Boolean verbose = false) {
        def functionJSON = describeFunction()
        String etag = getETag(functionJSON)

        String jsonTextOutput = steps.sh script: """
        aws cloudfront test-function \
        --name ${function} \
        --if-match ${etag} \
        --stage DEVELOPMENT \
        --event-object fileb://${testEventFilePath} \
        """, returnStdout: true

        if (verbose) {
            steps.echo jsonTextOutput
        }

        def jsonTestOutput = steps.readJSON text: jsonTextOutput

        assert jsonTestOutput.TestResult.FunctionErrorMessage == ""

        return jsonTestOutput.TestResult.FunctionOutput
    }

    public def publishFunction() {
        def functionJSON = describeFunction()
        String etag = getETag(functionJSON)

        steps.sh script: """
        aws cloudfront publish-function \
        --name ${function} \
        --if-match ${etag}
        """
    }

    private def describeFunction() {
        String textOutput = steps.sh script: "aws cloudfront describe-function --name ${function}", returnStdout: true
        def output = steps.readJSON text: textOutput
        return output
    }

    private String getETag(functionJSON) {
        return functionJSON.ETag
    }

    private def getFunctionConfig(functionJSON) {
        return functionJSON.FunctionSummary.FunctionConfig.toString()
    }
}