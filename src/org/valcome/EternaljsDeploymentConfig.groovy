package org.valcome

class EternaljsDeploymentConfig implements Serializable {

    def steps

    EternaljsDeploymentConfig(steps) {
        this.steps = steps
    }

    def parse() {
        if (!steps.env.DEPLOYMENT_CONFIG) {
            steps.error "No deployment configuration JSON provided"
        }

        steps.withFileParameter("DEPLOYMENT_CONFIG") {
            def deploymentConfigJSON = steps.readJSON file: steps.env.DEPLOYMENT_CONFIG
            return deploymentConfigJSON
        }
    }
}