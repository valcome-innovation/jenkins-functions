def call(project,
         app,
         zone,
         baseVersion = null,
         zoneVersion = null,
         closure) {
    withCredentials([[
            $class: 'VaultTokenCredentialBinding',
            credentialsId: "VAULT_APP_ROLE",
            vaultAddr: "https://vault.valcome.dev"
    ]]) {
        def fullEnv = [:]

        fullEnv.putAll(fetchEnvsFromVault(project, 'base', ".env", baseVersion))
        if (zone != null) {
            fullEnv.putAll(fetchEnvsFromVault(project, 'base', ".env.${zone}", baseVersion))
        }

        fullEnv.putAll(fetchEnvsFromVault(project, app, ".env", zoneVersion))
        if (zone != null) {
            fullEnv.putAll(fetchEnvsFromVault(project, app, ".env.${zone}", zoneVersion))
        }

        closure.delegate = [("VAULT_ENV"): fullEnv]
        closure()
    }
}

def fetchEnvsFromVault(project,
                       app,
                       env,
                       version = null) {
    def vaultTokenHeader = 'X-Vault-Token: $VAULT_TOKEN'
    def baseEnvPath = "/v1/env/data/$project/$app/$env"
    def envPath = version ? "$baseEnvPath?version=$version" : baseEnvPath
    def vaultUrl = '$VAULT_ADDR' + "$envPath"

    def textResponse = sh script: """curl -sS -X GET -H "$vaultTokenHeader" $vaultUrl""",
                 returnStdout: true
    def jsonResponse = readJSON text: textResponse
    return jsonResponse.data.data
}
