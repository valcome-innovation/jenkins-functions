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
            config = steps.readJSON file: steps.env.DEPLOYMENT_CONFIG
            return config
        }
    }

    String getProject() {
        return config.project
    }

    String getZone() {
        return config.zone
    }

    def getServices() {
        return config.services
    }

    def getService(String app) {
        return getServices().find { it.app == app }
    }

    def getServiceDeploymentConfig(String app) {
        return getService(app).deployment.collectEntries { key, value ->
            [(key.toString().toUpperCase()): value]
        }
    }
}