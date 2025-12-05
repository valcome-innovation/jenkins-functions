package org.valcome

class EternaljsDeployment implements Serializable {

    def steps
    String deploymentConfigPath

    EternaljsDeployment(steps,
                        String deploymentConfigPath) {
        this.steps = steps
        this.deploymentConfigPath = deploymentConfigPath
    }

    def deployAllServices() {
        this.validateRequiredJobParams()

        String base64Content = this.getBase64Config()

        steps.build job: 'eternal.js/service/deploy',
                parameters: [
                        steps.string(name: 'APP', value: null),
                        steps.string(name: 'VERSION', value: null),
                        steps.string(name: 'ZONE', value: "${steps.env.ZONE}"),
                        steps.string(name: 'HOST', value: null),
                        steps.string(name: 'SSH', value: null),
                        steps.string(name: 'BRANCH', value: "${steps.env.BRANCH}"),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    def deployService(String service) {
        this.validateRequiredJobParams()

        String base64Content = this.getBase64Config()

        steps.build job: 'eternal.js/service/deploy',
                parameters: [
                        steps.string(name: 'APP', value: "${service}"),
                        steps.string(name: 'VERSION', value: "${steps.env.VERSION}"),
                        steps.string(name: 'ZONE', value: "${steps.env.ZONE}"),
                        steps.string(name: 'HOST', value: null),
                        steps.string(name: 'SSH', value: null),
                        steps.string(name: 'BRANCH', value: "${steps.env.BRANCH}"),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    def deployUnit(String unit) {
        String base64Content = this.getBase64Config()

        steps.build job: "eternal.js/${unit}/deploy",
                parameters: [
                        steps.string(name: 'HOST', value: null),
                        steps.string(name: 'SSH', value: null),
                        steps.string(name: 'BRANCH', value: "${steps.env.BRANCH}"),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    String getBase64Config() {
        String deploymentConfigContent = steps.readFile file: deploymentConfigPath
        return Base64.encoder.encodeToString(deploymentConfigContent.bytes)
    }

    def validateRequiredJobParams() {
        if (!steps.env.ZONE) {
            steps.error "ZONE not defined"
        }
    }
}