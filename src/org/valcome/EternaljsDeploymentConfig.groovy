package org.valcome

class EternaljsDeploymentConfig implements Serializable {

    def steps
    def config

    EternaljsDeploymentConfig(steps) {
        this.steps = steps
    }

    def parse() {
        if (!steps.env.DEPLOYMENT_CONFIG) {
            steps.error "No deployment configuration JSON provided"
        }

        steps.withFileParameter("DEPLOYMENT_CONFIG") {
            this.config = steps.readJSON file: steps.env.DEPLOYMENT_CONFIG
            return this.config
        }
    }

    String getProject() {
        return this.config.project
    }

    String getZone() {
        return this.config.zone
    }

    def getServices() {
        return this.config.services
    }

    def getService(String app) {
        return this.getServices().find { it.app == app }
    }

    def getServiceDeploymentConfig(String app) {
        return this.getService(app).deployment.collectEntries { key, value ->
            [(key.toString().toUpperCase()): value]
        }
    }
}