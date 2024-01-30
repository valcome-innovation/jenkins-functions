package org.valcome

class EternaljsDeployment implements Serializable {

    def steps
    def deploymentConfigJson
    String deploymentConfigContent

    EternaljsDeployment(steps,
                        String deploymentConfigPath) {
        this.steps = steps
        this.deploymentConfigJson = steps.readJSON file: deploymentConfigPath
        this.deploymentConfigContent = steps.readFile file: deploymentConfigPath
        this.validateRequiredJobParams()
    }

    String getBase64Config() {
        return Base64.encoder.encodeToString(this.deploymentConfigContent.bytes)
    }

    def deployService(String service) {
        steps.build job: 'eternal.js/service/deploy',
                parameters: [
                        steps.string(name: 'APP', value: "${service}"),
                        steps.string(name: 'VERSION', value: "${VERSION}"),
                        steps.string(name: 'ZONE', value: "${ZONE}"),
                        steps.string(name: 'HOST', value: "${HOST}"),
                        steps.string(name: 'SSH', value: "${SSH}"),
                        steps.string(name: 'VAULT_APP_ROLE_SECRET_ID', value: "${VAULT_CREDENTIALS_ID}"),
                        steps.string(name: 'PROJECT', value: "${this.deploymentConfigJson.project}"),
                        steps.string(name: 'BRANCH', value: 'main'),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: this.getBase64Config())
                ]
    }

    def validateRequiredJobParams() {
        if (!HOST) {
            steps.error "HOST not defined"
        }
        if (!VERSION) {
            steps.error "VERSION not defined"
        }
        if (!ZONE) {
            steps.error "ZONE not defined"
        }
        if (!SSH) {
            steps.error "SSH not defined"
        }
        if (!VAULT_CREDENTIALS_ID) {
            steps.error "VAULT_CREDENTIALS_ID not defined"
        }
    }
}