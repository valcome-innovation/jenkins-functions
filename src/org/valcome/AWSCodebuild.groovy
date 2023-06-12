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

    def triggerBuildAndAwait(remote) {
        def build = startBuild(remote)
        awaitBuild(remote, build.id)
    }

    def startBuild(remote) {
        def customCommand = """
            aws codebuild start-build-batch \
                --project-name ${buildParams.project} \
                --source-version refs/heads/${buildParams.branch} \
                --environment-variables-override
        """

        if (buildParams.app != null) {
            customCommand = customCommand.concat(" name=APP,value=${buildParams.app} ")
        }

        if (buildParams.version != null) {
            customCommand = customCommand.concat(" name=TAG,value=${buildParams.version} ")
        }

        if (buildParams.environment != null) {
            customCommand = customCommand.concat(" name=ENVIRONMENT,value=${buildParams.app} ")
        }

        def result = steps.sh "${customCommand}"
        // def result = steps.sshCommand remote: remote, command: customCommand
        def json = steps.readJSON text: "" + result
        return json.buildBatch
    }

    def awaitBuild(remote, build_id) {
        def runningBuild = getBuildStatus(remote, build_id)

        while (runningBuild.buildBatchStatus == "IN_PROGRESS") {
            sleep 15
            runningBuild = getBuildStatus(remote, build_id)
            steps.echo "wait 15 seconds for next poll..."
        }

        if (runningBuild.buildBatchStatus == "STOPPED") {
            steps.currentBuild.result = 'ABORTED'
        } else if (runningBuild.buildBatchStatus != "SUCCEEDED") {
            throw new Exception('AWS Code Build failed (https://eu-central-1.console.aws.amazon.com/codesuite/codebuild/projects?region=eu-central-1)')
        }
    }

    def getBuildStatus(remote, build_id) {
        def resultFile = "${steps.env.WORKSPACE}/${buildNumber}_build-status.json"
        // def result = steps.sshCommand remote: remote,
        //         command: "aws codebuild batch-get-build-batches --ids ${build_id} > ${remoteFilePath}"

        def result = steps.sh "aws codebuild batch-get-build-batches --ids ${build_id} > ${resultFile}"
        def fileContent = steps.readFile "${resultFile}"
        steps.sh "rm ${resultFile}"

        def json = steps.readJSON text: "" + fileContent
        def runningBuild = json.buildBatches[0]
        steps.echo "Phase: " + runningBuild.currentPhase
        steps.echo "Status: " + runningBuild.buildBatchStatus

        return runningBuild
    }
}