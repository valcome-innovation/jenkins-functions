package org.valcome

class EternaljsDeployment implements Serializable {

    def steps
    String deploymentConfigPath

    EternaljsDeployment(steps,
                        String deploymentConfigPath) {
        this.steps = steps
        this.deploymentConfigPath = deploymentConfigPath

    }

    def deployService(String service) {
        this.validateRequiredJobParams()

        def deploymentConfigJson = steps.readJSON file: deploymentConfigPath
        String deploymentConfigContent = steps.readFile file: deploymentConfigPath
        String base64Content = this.getBase64Config(deploymentConfigContent)

        steps.build job: 'eternal.js/service/deploy',
                parameters: [
                        steps.string(name: 'APP', value: "${service}"),
                        steps.string(name: 'VERSION', value: "${VERSION}"),
                        steps.string(name: 'ZONE', value: "${ZONE}"),
                        steps.string(name: 'HOST', value: "${HOST}"),
                        steps.string(name: 'SSH', value: "${SSH}"),
                        steps.string(name: 'VAULT_APP_ROLE_SECRET_ID', value: "${VAULT_CREDENTIALS_ID}"),
                        steps.string(name: 'PROJECT', value: "${deploymentConfigJson.project}"),
                        steps.string(name: 'BRANCH', value: 'main'),
                        steps.base64File(name: 'DEPLOYMENT_CONFIG', base64: base64Content)
                ]
    }

    String getBase64Config(String deploymentConfigContent) {
        return Base64.encoder.encodeToString(deploymentConfigContent.bytes)
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