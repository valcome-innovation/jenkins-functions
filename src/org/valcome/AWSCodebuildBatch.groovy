package org.valcome

class AWSCodebuildBatch implements Serializable {
    def buildParams
    def steps

    AWSCodebuildBatch(steps = null,
                      project = null,
                      branch = 'main',
                      version = null,
                      app = null,
                      environment = null,
                      skipTests = false,
                      skipSonar = true,
                      skipPublish = false) {
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

    def triggerBuildBatchAndAwait() {
        def batch = startBuildBatch()
        awaitBuildBatch(batch.id)
    }

    def startBuildBatch() {
        def customCommand = """
        aws codebuild start-build-batch \
        --project-name ${buildParams.project} \
        --source-version ${buildParams.branch} \
        --environment-variables-override \
        name=TAG,value=${buildParams.version} """

        if (buildParams.app != null) {
            customCommand += " name=APP,value=${buildParams.app} "
        }

        if (buildParams.environment != null) {
            customCommand += " name=ENVIRONMENT,value=${buildParams.environment} "
        }

        if (buildParams.skipTests != null) {
            customCommand += " name=SKIP_TESTS,value=${buildParams.skipTests} "
        }

        if (buildParams.skipSonar != null) {
            customCommand += " name=SKIP_SONAR,value=${buildParams.skipSonar} "
        }

        if (buildParams.skipPublish != null) {
            customCommand += " name=SKIP_PUBLISH,value=${buildParams.skipPublish} "
        }

        def result = steps.sh script: "${customCommand}", returnStdout: true
        def json = steps.readJSON text: "" + result
        return json.buildBatch
    }

    def awaitBuildBatch(batch_id) {
        def endPhase = ['SUCCEEDED', 'COMPLETED', 'STOPPED', 'FAILED']
        def runningBatch = getBuildBatchStatus(batch_id)

        while (!endPhase.contains(runningBatch.buildBatchStatus)) {
            steps.sleep 15
            runningBatch = getBuildBatchStatus(batch_id)
            reportBuildStatus(runningBatch)
        }

        def endStatus = runningBatch.buildBatchStatus
        updateBuildResult(endStatus)

        if (!buildParams.skipSonar) {
            evaluateSonarStatus(buildParams.project)
        }

        steps.echo "Build Finished: ${steps.currentBuild.result}"
    }

    def getBuildBatchStatus(batch_id) {
        def result = steps.sh script: "aws codebuild batch-get-build-batches --ids ${batch_id}", returnStdout: true
        def json = steps.readJSON text: "" + result
        def runningBatch = json.buildBatches[0]
        steps.echo "Phase: ${runningBatch.currentPhase}, Status: ${runningBatch.buildBatchStatus}"

        return runningBatch
    }

    def reportBuildStatus(runningBatch) {
        // maps codebuild state to github status
        def statusMap = [
            PENDING: "QUEUED",
            FAILED: "COMPLETED",
            FAULT: "COMPLETED",
            IN_PROGRESS: "IN_PROGRESS",
            STOPPED: "COMPLETED",
            SUCCEEDED: "COMPLETED",
            TIMED_OUT: "COMPLETED",
            SKIPPED: "COMPLETED"
        ]

        def conclusionMap = [
            PENDING: "NONE",
            IN_PROGRESS: "NONE",
            STOPPED: "CANCELED",
            FAILED: "FAILURE",
            FAULT: "FAILURE",
            SUCCEEDED: "SUCCESS",
            TIMED_OUT: "TIME_OUT",
            SKIPPED: "NEUTRAL"
        ]

        def buildGroups = runningBatch.buildGroups
        def donwloadSource = buildGroups.find { it.identifier == "DOWNLOAD_SOURCE" };

        if (donwloadSource.currentBuildSummary.buildStatus == 'SUCCEEDED') {
            def buildSteps = buildGroups.findAll { it.identifier != "DOWNLOAD_SOURCE" };

            for (buildStep in buildSteps) {
                def isSkipped = isSkipped(buildStep)
                def buildStatus = isSkipped ? "SKIPPED" : buildStep.currentBuildSummary.buildStatus
                def name = getBuildStepName(buildStep.identifier)
                def title = "Status: ${buildStatus}"
                def status = statusMap[buildStatus]
                def conclusion = conclusionMap[buildStatus]

                if (!isSkipped && isPullRequest()) {
                    def detailsURL = getDetailsUrl(name, runningBatch)
                    steps.publishGithubCheck(name, title, status, conclusion, buildStatus, "", detailsURL)
                } else {
                    steps.echo "${name} is ${status}, conclusion is ${conclusion}"
                }
            }
        }
    }

    boolean isSkipped(buildStep) {
        def title = buildStep.identifier.toLowerCase()
        return (title.contains('test') && buildParams.skipTests) ||
               (title.contains('sonar') && buildParams.skipSonar) ||
               (title.contains('publish') && buildParams.skipPublish)
    }

    boolean isPullRequest() {
        return steps.env.CHANGE_ID != null
    }

    String getBuildStepName(String identifier) {
        String name = identifier.replaceAll("_", " ").capitalize()

        return name != 'Build' || buildParams.app == null
                ? name
                : "${name} ${buildParams.app} "
    }

    def getDetailsUrl(title, batch) {
        if (title.toLowerCase().contains("sonar")) {
            return "https://sonar.valcome.dev/dashboard?id=${batch.projectName}"
        } else {
            return "https://eu-central-1.console.aws.amazon.com/codesuite/codebuild/487554623251/projects/${batch.projectName}/batch/${batch.id}?region=eu-central-1"
        }
    }

    def updateBuildResult(endStatus) {
        if (endStatus == "STOPPED") {
            steps.currentBuild.result = 'ABORTED'
            steps.error("Build aborted")
        } else if (endStatus == "COMPLETED") {
            steps.currentBuild.result = 'UNSTABLE'
            steps.error("Build unstable")
        } else if (endStatus == "FAILED") {
            steps.currentBuild.result = 'FAILURE'
            steps.error("Build failed")
        } else {
            steps.currentBuild.result = 'SUCCESS'
        }
    }
}
