package org.valcome

class AWSCodebuild implements Serializable {
    def buildParams
    def steps

    AWSCodebuild(steps = null, project = null, branch = 'main', version = null, app = null, environment = null) {
        this.steps = steps
        this.buildParams = [
            project: project,
            branch: branch,
            version: version,
            app: app,
            environment: environment
        ]
    }

    def triggerBuild(remote) {
        def build = startBuild(remote)
        def runningBuild = getBuildStatus(remote, build.id)

        while (runningBuild.buildBatchStatus == "IN_PROGRESS") {
            sleep 15
            runningBuild = getBuildStatus(remote, build.id)
            steps.echo "Phase: " + runningBuild.currentPhase
            steps.echo "Status: " + runningBuild.buildBatchStatus
            steps.echo "wait 15 seconds for next poll..."
        }

        if (runningBuild.buildBatchStatus == "STOPPED") {
            steps.currentBuild.result = 'ABORTED'
        } else if (runningBuild.buildBatchStatus != "SUCCEEDED") {
            throw new Exception('AWS Code Build failed (https://eu-central-1.console.aws.amazon.com/codesuite/codebuild/projects?region=eu-central-1)')
        }
    }

    def startBuild(remote) {
        def customCommand = """
            aws codebuild start-build-batch \
                --project-name ${buildParams.project} \
                --source-version refs/heads/${buildParams.branch} \
                --environment-variables-override
        """

        if (buildParams.app != null) {
            customCommand.concat(" name=APP,value=${buildParams.app} ")
        }

        if (buildParams.version != null) {
            customCommand.concat(" name=TAG,value=${buildParams.version} ")
        }

        if (buildParams.environment != null) {
            customCommand.concat(" name=ENVIRONMENT,value=${buildParams.app} ")
        }


        def result = steps.sshCommand remote: remote, command: customCommand
        def json = steps.readJSON text: "" + result
        return json.buildBatch
    }

    def getBuildStatus(remote, build_id) {
        def result = steps.sshCommand remote: remote,
                command: "aws codebuild batch-get-build-batches --ids ${build_id}"

        def json = steps.readJSON text: "" + result
        return json.buildBatches[0]
    }

}