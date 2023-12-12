package org.valcome

class NgLiveDeployment implements Serializable {
    def steps
    def params

    NgLiveDeployment(
                    steps = null,
                    branch = 'main',
                    project = null,
                    env = 'dev') {
        this.steps = steps
        this.params = [
            project: project,
            branch: branch,
            env: env,
            webHosts: null,
            adminHosts: null,
            webSSH: null,
            adminSSH: null
        ]
    }

    def onWebHosts(hosts, ssh) {
        params.webHosts = extractHosts(hosts)
        params.webSSH = ssh
        return this;
    }

    def onAdminHosts(hosts, ssh) {
        params.adminHosts = extractHosts(hosts)
        params.adminSSH = ssh
        return this;
    }

    private def extractHosts(hosts) {
        return "${hosts}".split('\n')
    }

    def deployWeb(web_version, server_version, saleor_version, dashboard_version) {
        for (String host : params.webHosts) {
            deployOnWebNode(host, web_version, server_version, saleor_version, dashboard_version)
        }
    }


    private def deployOnWebNode(host, web_version, server_version, saleor_version, dashboard_version) {
        steps.withSshRemote(params.webSSH, host, 'REMOTE') {
            steps.echo "Starting live deploy on: ${host}"

            steps.gitForcePull(REMOTE, '~/git/ng-live-suite/ ', params.branch)
            deployLiveOn(REMOTE, web_version, server_version, saleor_version, dashboard_version)

            steps.echo "Finished live deploy on: ${host}"
        }
    }

    private def deployLiveOn(remote, web_version, server_version, saleor_version, dashboard_version) {
        steps.sshCommand remote: remote,
                command: """
                    cd ~/git/ng-live-suite/ && \
                    LIVE_WEB_VERSION=${web_version} \
                    LIVE_SERVER_VERSION=${server_version} \
                    SALEOR_API_VERSION=${saleor_version} \
                    SALEOR_DASHBOARD_VERSION=${dashboard_version} \
                    sh ~/git/ng-live-suite/ci/deploy/${params.project}/${params.env}/deploy.sh
                """
    }


    def deployAdmin(admin_version, server_version) {
        for (String host : params.adminHosts) {
            deployOnAdminNode(host, admin_version, server_version)
        }
    }

    private def deployOnAdminNode(host, admin_version, server_version) {
        steps.withSshRemote(params.adminSSH, host, 'REMOTE') {
            steps.echo "Starting admin deploy"

            steps.gitForcePull(REMOTE, '~/git/ng-live-suite/ ', params.branch)
            deployAdminOn(REMOTE, admin_version, server_version)

            steps.echo "Finished admin deploy"
        }
    }

    private def deployAdminOn(remote, admin_version, server_version) {
        steps.sshCommand remote: remote,
                command: """
                    cd ~/git/ng-live-suite/ && \
                    LIVE_ADMIN_DASHBOARD_VERSION=${admin_version} \
                    LIVE_SERVER_VERSION=${server_version} \
                    sh ~/git/ng-live-suite/ci/deploy/${params.project}/${params.env}/deploy-admin.sh
                """
    }

    def triggerE2eTests() {
        def e2eTestJob = steps.build job: '/ng-live/e2e/web',
            quietPeriod: 60,
            wait: true,
            propagate: false,
            parameters: [
                steps.string(name: 'branch', value: params.branch),
                steps.string(name: 'project', value: params.project),
                steps.string(name: 'environment', value: 'ci'),
                steps.string(name: 'host', value: params.webHosts[0])
            ]

        if (e2eTestJob.getResult().isWorseThan(Result.SUCCESS)) {
            steps.currentBuild.result = 'UNSTABLE'
        }
    }
}
