def call(project,
         app,
         zone) {
    withCredentials([[
        $class: 'VaultTokenCredentialBinding',
        credentialsId: "VAULT_APP_ROLE",
        vaultAddr: 'https://vault.valcome.dev'
    ]]) {
        return fetchEnvsFromVault(project, app, ".env")
    }
}

def fetchEnvsFromVault(project, app, env) {
    def res = sh script: '''
    curl \
    -X GET \
    -H "X-Vault-Token: $VAULT_TOKEN" \
    $VAULT_ADDR/v1/env/data/$project/$app/$env
    ''', returnStdout: true
    def jsonRes = readJSON text: res
    return jsonRes.data.data
}
