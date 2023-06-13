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
                 skipSonar = 'true',
                 skipPublish = null) {
        this.steps = steps
        this.buildParams = [
            project: project,
            branch: branch,
            version: version,
            app: app,
            environment: environment,
            skipTests: skipTests,
            skipSonar: skipSonar,
            skipPublish: skipPublish
        ]
    }

    def triggerBuildAndAwait() {
        def build = startBuild()
        awaitBuild(build.id)
    }

    def startBuild() {
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

        if (buildParams.skipPublish != null) {
            customCommand += " name=SKIP_PUBLISH,value=${buildParams.skipPublish} "
        }

        customCommand += " name=TAG,value=${buildParams.version} "

        def result = steps.sh script: "${customCommand}", returnStdout: true
        def json = steps.readJSON text: "" + result
        return json.buildBatch
    }

    def awaitBuild(build_id) {
        def endPhase = ['SUCCEEDED', 'COMPLETED', 'STOPPED', 'FAILED']
        def runningBuild = getBuildStatus(build_id)
        def batchStatus = runningBuild.buildBatchStatus

        while (!endPhase.contains(runningBuild.currentPhase)) {
            steps.sleep 15
            runningBuild = getBuildStatus(build_id)
            reportBuildStatus(runningBuild)
        }

        if (endPhase == "STOPPED") {
            steps.currentBuild.result = 'ABORTED'
        } else if (endPhase == "COMPLETED") {
            steps.currentBuild.result = 'UNSTABLE'
        } else if (endPhase == "SUCCEEDED") {
            steps.currentBuild.result = 'FAILURE'
        } else {
            steps.currentBuild.result = 'SUCCESS'
        }
    }

    def getBuildStatus(build_id) {
        def result = steps.sh script: "aws codebuild batch-get-build-batches --ids ${build_id}", returnStdout: true
        def json = steps.readJSON text: "" + result
        def runningBuild = json.buildBatches[0]
        steps.echo "Phase: ${runningBuild.currentPhase}, Status: ${runningBuild.buildBatchStatus}"

        return runningBuild
    }

    def reportBuildStatus(runningBuild) {
        // maps codebuild state to github status
        def statusMap = [
            PENDING: "QUEUED",
            FAILED: "COMPLETED",
            FAULT: "COMPLETED",
            IN_PROGRESS: "IN_PROGRESS",
            STOPPED: "COMPLETED",
            SUCCEEDED: "COMPLETED",
            TIMED_OUT: "COMPLETED"
        ]

        def conclusionMap = [
            PENDING: "NONE",
            IN_PROGRESS: "NONE",
            STOPPED: "CANCELED",
            FAILED: "FAILURE",
            FAULT: "FAILURE",
            SUCCEEDED: "SUCCESS",
            TIMED_OUT: "TIME_OUT"
        ]

        def buildGroups = runningBuild.buildGroups
        def donwloadSource = buildGroups.find { it.identifier == "DOWNLOAD_SOURCE" };

        if (donwloadSource.currentBuildSummary.buildStatus == 'SUCCEEDED') {
            def buildSteps = buildGroups.findAll { it.identifier != "DOWNLOAD_SOURCE" };

            for (buildStep in buildSteps) {
                def ignoreFailure = buildStep.ignoreFailure
                def status = statusMap[buildStep.currentBuildSummary.buildStatus]
                def conclusion = conclusionMap[buildStep.currentBuildSummary.buildStatus]
                def title = buildStep.identifier.replaceAll("_", " ").capitalize()

                if (steps.env.CHANGE_ID != null) {
                    steps.publishGithubCheck(title, title, status, conclusion)
                } else {
                    steps.echo "${title} is ${status}, conclusion is ${conclusion}"
                }
            }
        }
    }
}
