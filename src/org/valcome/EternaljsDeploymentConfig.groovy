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

    def getDatabase() {
        return this.config.database
    }

    def getDatabaseConfig(String app) {
        this.config.database.find { it.app == app }
    }

    String getDatabaseVersion(String app) {
        return this.getDatabaseConfig(app).appVersion
    }

    def getDatabaseDeploymentConfig(String app) {
        return this.getDatabaseConfig(app).deployment.collectEntries { key, value ->
            [(key.toString().toUpperCase()): value]
        }
    }

    def getServices() {
        return this.config.services
    }

    def getServiceConfig(String app) {
        return this.getServices().find { it.app == app }
    }

    String getServiceVersion(String app) {
        return this.getServiceConfig(app).appVersion
    }

    def getServiceDeploymentConfig(String app) {
        return this.getServiceConfig(app).deployment.collectEntries { key, value ->
            [(key.toString().toUpperCase()): value]
        }
    }
}