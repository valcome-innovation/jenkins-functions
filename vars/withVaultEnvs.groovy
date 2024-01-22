def call(project,
         app,
         zone,
         closure) {
    withCredentials([[
            $class: 'VaultTokenCredentialBinding',
            credentialsId: "VAULT_APP_ROLE",
            vaultAddr: "https://vault.valcome.dev"
    ]]) {
        def baseEnv = fetchEnvsFromVault(project, 'base', ".env")
        def baseEnvZone = fetchEnvsFromVault(project, 'base', ".env.${zone}")
        def appEnv = fetchEnvsFromVault(project, app, ".env")
        def appEnvZone = fetchEnvsFromVault(project, app, ".env.${zone}")

        def fullEnv = baseEnv
        fullEnv.putAll(baseEnvZone)
        fullEnv.putAll(appEnv)
        fullEnv.putAll(appEnvZone)

        closure.delegate = [("VAULT_ENV"): fullEnv]
        closure()
    }
}

def fetchEnvsFromVault(project,
                       app,
                       env) {
    def vaultTokenHeader = 'X-Vault-Token: $VAULT_TOKEN'
    def vaultUrl = '$VAULT_ADDR' + "/v1/env/data/$project/$app/$env"

    def textResponse = sh script: """curl -X GET -H "$vaultTokenHeader" $vaultUrl""",
                 returnStdout: true
    def jsonResponse = readJSON text: textResponse
    return jsonResponse.data.data
}
