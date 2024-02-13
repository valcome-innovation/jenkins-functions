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
                        steps.string(name: 'HOST', value: "${steps.env.HOST}"),
                        steps.string(name: 'SSH', value: "${steps.env.SSH}"),
                        steps.string(name: 'VAULT_APP_ROLE_SECRET_ID', value: "${steps.env.VAULT_CREDENTIALS_ID}"),
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
                        steps.string(name: 'HOST', value: "${steps.env.HOST}"),
                        steps.string(name: 'SSH', value: "${steps.env.SSH}"),
                        steps.string(name: 'VAULT_APP_ROLE_SECRET_ID', value: "${steps.env.VAULT_CREDENTIALS_ID}"),
                        steps.string(name: 'BRANCH', value: "${steps.env.BRANCH}"),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    def deployDatabase() {
        String base64Content = this.getBase64Config()

        steps.build job: 'eternal.js/db/deploy',
                parameters: [
                        steps.string(name: 'HOST', value: "${steps.env.HOST}"),
                        steps.string(name: 'SSH', value: "${steps.env.SSH}"),
                        steps.string(name: 'BRANCH', value: "${steps.env.BRANCH}"),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    String getBase64Config() {
        String deploymentConfigContent = steps.readFile file: deploymentConfigPath
        return Base64.encoder.encodeToString(deploymentConfigContent.bytes)
    }

    def validateRequiredJobParams() {
        if (!steps.env.HOST) {
            steps.error "HOST not defined"
        }
        if (!steps.env.ZONE) {
            steps.error "ZONE not defined"
        }
        if (!steps.env.SSH) {
            steps.error "SSH not defined"
        }
        if (!steps.env.VAULT_CREDENTIALS_ID) {
            steps.error "VAULT_CREDENTIALS_ID not defined"
        }
    }
}