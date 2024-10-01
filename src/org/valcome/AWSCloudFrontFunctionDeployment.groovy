package org.valcome

class AWSCloudFrontFunctionDeployment implements Serializable {
    def steps
    String function

    AWSCloudFrontFunctionDeployment(steps = null,
                                    function = null) {
        this.function = function
    }

    def uploadFunction(String functionCodeFilePath) {
        def functionJSON = describeFunction()
        String etag = getETag(functionJSON)
        def functionConfig = getFunctionConfig(functionJSON)

        steps.sh script: """
        aws cloudfront update-function \
        --name ${function} \
        --if-match ${etag} \
        --function-code fileb://${functionCodePath} \
        --function-config '${functionConfig}'
        """
    }

    def runTestEvent(String testEventFilePath) {
        def functionJSON = describeFunction()
        String etag = getETag(functionJSON)

        String jsonTextOutput = steps.sh script: """
        aws cloudfront test-function \
        --name ${function} \
        --if-match ${etag} \
        --stage DEVELOPMENT \
        --event-object fileb://${testEventFilePath} \
        """, returnStdout: true
        def jsonTestOutput = steps.readJSON text: jsonTextOutput
        assert jsonTestOutput.FunctionErrorMessage == ""
        return jsonTestOutput
    }

    private def describeFunction() {
        String textOutput = steps.sh script: "aws cloudfront describe-function --name ${function}, returnStdout: true"
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