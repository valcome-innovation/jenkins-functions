package org.valcome

class AWSCodebuild implements Serializable {
    def buildParams
    def steps

    AWSCodebuild(steps = null,
                 project = null,
                 branch = 'main',
                 version = null,
                 skipPublish = null) {
        this.steps = steps
        this.buildParams = [
            project: project,
            branch: branch,
            version: version,
            skipPublish: skipPublish
        ]
    }

    def triggerBuildAndAwait() {
        def build = startBuild()
        awaitBuild(build.id)
    }

    def startBuild() {
        def customCommand = """
        aws codebuild start-build \
        --project-name ${buildParams.project} \
        --source-version ${buildParams.branch} \
        --environment-variables-override \
        name=TAG,value=${buildParams.version}"""

        if (buildParams.skipPublish != null) {
            customCommand += " name=SKIP_PUBLISH,value=${buildParams.skipPublish} "
        }

        def result = steps.sh script: "${customCommand}", returnStdout: true
        def json = steps.readJSON text: "" + result
        return json.build
    }

    def awaitBuild(build_id) {
        if (isPullRequest()) {
            steps.publishGithubCheck("Verify", "Verify", "IN_PROGRESS", "NONE")
        }

        def endPhase = ['SUCCEEDED', 'COMPLETED', 'STOPPED', 'FAILED']
        def runningBuild = getBuildStatus(build_id)

        while (!endPhase.contains(runningBuild.currentPhase)) {
            steps.echo "Phase: ${runningBuild.currentPhase}, Status: ${runningBuild.buildStatus}"
            steps.sleep 15
            runningBuild = getBuildStatus(build_id)
        }

        def endStatus = runningBuild.buildStatus

        if (isPullRequest()) {
            steps.publishGithubCheck("Verify", "Verify", endStatus, getConclusion(endStatus))
        }

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

        steps.echo "Build Finished: ${steps.currentBuild.result}"
    }

    def getBuildStatus(build_id) {
        def result = steps.sh script: "aws codebuild batch-get-builds --ids ${build_id}", returnStdout: true
        def json = steps.readJSON text: "" + result
        def runningBuild = json.builds[0]
        return runningBuild
    }

    boolean isPullRequest() {
        return steps.env.CHANGE_ID != null
    }

    String getConclusion(String buildStatus) {
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

        return conclusionMap[buildStatus]
    }
}
