def call(project,
         app,
         zone,
         closure) {
    withCredentials([[
            $class: 'VaultTokenCredentialBinding',
            credentialsId: "VAULT_APP_ROLE",
            vaultAddr: "https://vault.valcome.dev"
    ]]) {
        def fullEnv = [:]

        fullEnv.putAll(fetchEnvsFromVault(project, 'base', ".env"))
        if (zone != null) {
            fullEnv.putAll(fetchEnvsFromVault(project, 'base', ".env.${zone}"))
        }

        fullEnv.putAll(fetchEnvsFromVault(project, app, ".env"))
        if (zone != null) {
            fullEnv.putAll(fetchEnvsFromVault(project, app, ".env.${zone}"))
        }

        closure.delegate = [("VAULT_ENV"): fullEnv]
        closure()
    }
}

def fetchEnvsFromVault(project,
                       app,
                       env) {
    def vaultTokenHeader = 'X-Vault-Token: $VAULT_TOKEN'
    def vaultUrl = '$VAULT_ADDR' + "/v1/env/data/$project/$app/$env"

    def textResponse = sh script: """curl -sS -X GET -H "$vaultTokenHeader" $vaultUrl""",
                 returnStdout: true
    def jsonResponse = readJSON text: textResponse
    return jsonResponse.data.data
}
