package org.valcome


class EternaljsDeploymentConfig implements Serializable {

    def steps
    def config

    EternaljsDeploymentConfig(steps) {
        this.steps = steps
    }

    EternaljsDeploymentConfig parse() {
        if (!steps.env.DEPLOYMENT_CONFIG) {
            steps.error "No deployment configuration JSON provided"
        }

        steps.withFileParameter("DEPLOYMENT_CONFIG") {
            this.config = steps.readJSON file: steps.env.DEPLOYMENT_CONFIG
            return this
        }
    }

    String getProject() {
        return this.config.project
    }

    String getZone() {
        return this.config.zone
    }

    def eternaljs() {
        return this.config.eternaljs
    }

    def getDatabase() {
        return this.eternaljs().database
    }

    def getDatabaseConfig(String app) {
        this.getDatabase().find { it.app == app }
    }

    String getDatabaseVersion(String app) {
        return this.getDatabaseConfig(app).appVersion
    }

    def getDatabaseEnvVersions(String app) {
        return this.getDatabaseConfig(app).envVersions
    }

    def getDatabaseDeploymentConfig(String app) {
        def config = this.getDatabaseConfig(app).deployment

        return toConfigMap(config)
    }

    def getServices() {
        return this.eternaljs().services
    }

    def getServiceConfig(String app) {
        return this.getServices().find { it.app == app }
    }

    String getServiceVersion(String app) {
        return this.getServiceConfig(app).appVersion
    }

    def getServiceDeploymentConfig(String app) {
        def config = this.getServiceConfig(app).deployment

        return toConfigMap(config)
    }

    def getServiceEnvVersions(String app) {
        return this.getServiceConfig(app).envVersions
    }

    def getBaseEnvVersions() {
        return this.eternaljs().envVersions
    }

    String getSSHCredentialId() {
        return this.eternaljs().ssh.credentialsId
    }

    String getSSHHost() {
        return this.eternaljs().ssh.host
    }

    static Map toConfigMap(config) {
        return config.collectEntries { key, value ->
            [(key.toString().toUpperCase()): value]
        }
    }
}