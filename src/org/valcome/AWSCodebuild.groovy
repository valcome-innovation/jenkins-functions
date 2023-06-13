package org.valcome

class AWSCodebuild implements Serializable {
    def buildParams
    def steps

    AWSCodebuild(steps = null,
                 project = null,
                 branch = 'main',
                 version = null,
                 app = null,
                 environment = null,
                 skipTests = 'false',
                 skipSonar = 'true') {
        this.steps = steps
        this.buildParams = [
            project: project,
            branch: branch,
            version: version,
            app: app,
            environment: environment,
            skipTests: skipTests,
            skipSonar: skipSonar
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
        --source-version ${buildParams.branch} \
        --environment-variables-override \
        name=SKIP_TESTS,value=${buildParams.skipTests} \
        name=SKIP_SONAR,value=${buildParams.skipSonar} """

        if (buildParams.app != null) {
            customCommand += " name=APP,value=${buildParams.app} "
        }

        if (buildParams.environment != null) {
            customCommand += " name=ENVIRONMENT,value=${buildParams.environment} "
        }

        customCommand += " name=TAG,value=${buildParams.version} "

        def result = steps.sh script: "${customCommand}", returnStdout: true
        def json = steps.readJSON text: "" + result
        return json.buildBatch
    }

    def awaitBuild(remote, build_id) {
        def runningBuild = getBuildStatus(remote, build_id)

        while (runningBuild.buildBatchStatus == "IN_PROGRESS") {
            steps.sleep 15
            runningBuild = getBuildStatus(remote, build_id)
            updateGithubChecks(runningBuild)
            steps.echo "wait 15 seconds for next poll..."
        }

        if (runningBuild.buildBatchStatus == "STOPPED") {
            steps.currentBuild.result = 'ABORTED'
        } else if (runningBuild.buildBatchStatus != "SUCCEEDED") {
            throw new Exception('AWS Code Build failed (https://eu-central-1.console.aws.amazon.com/codesuite/codebuild/projects?region=eu-central-1)')
        }
    }

    def getBuildStatus(remote, build_id) {
        def resultFile = "${steps.env.WORKSPACE}/build-status.json"
        steps.sh "aws codebuild batch-get-build-batches --ids ${build_id} > ${resultFile}"
        def fileContent = steps.readFile "${resultFile}"
        steps.sh "rm ${resultFile}"

        def json = steps.readJSON text: "" + fileContent
        def runningBuild = json.buildBatches[0]
        steps.echo "Phase: ${runningBuild.currentPhase}, Status: ${runningBuild.buildBatchStatus}"

        return runningBuild
    }

    def updateGithubChecks(runningBuild) {
        // maps codebuild state to github status
        def statusMap = [
            FAILED: "COMPLETED",
            FAULT: "COMPLETED",
            IN_PROGRESS: "IN_PROGRESS",
            STOPPED: "COMPLETED",
            SUCCEEDED: "COMPLETED",
            TIMED_OUT: "COMPLETED"
        ]

        def conclusionMap = [
            FAILED: "FAILURE",
            FAULT: "FAILURE",
            IN_PROGRESS: "NONE",
            STOPPED: "CANCELLED",
            SUCCEEDED: "SUCCESS",
            TIMED_OUT: "TIMED_OUT"
        ]

        // if (steps.env.CHANGE_ID != null) {
            def buildGroups = runningBuild.buildGroups
            def donwloadSource = buildGroups.find { it.identifier == "DOWNLOAD_SOURCE" };

            if (donwloadSource.currentBuildSummary.buildStatus == 'SUCCEEDED') {
                def buildSteps = buildGroups.findAll { it.identifier != "DOWNLOAD_SOURCE" };

                for (buildStep in buildSteps) {
                    def status = statusMap[buildStep.currentBuildSummary.buildStatus]
                    def conclusion = conclusionMap[buildStep.currentBuildSummary.buildStatus]
                    steps.echo buildStep.identifier
                    steps.echo status
                    steps.echo conclusion
                    steps.publishGithubCheck(buildStep.identifier, buildStep.identifier, status, conclusion)
                }
            }
        // }
    }
}
