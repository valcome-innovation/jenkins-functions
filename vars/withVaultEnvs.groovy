def call(SECRET_ID,
         PROJECT,
         APP,
         ZONE,
         ENV_VARS) {

    def configuration = [
        vaultUrl: 'https://vault.valcome.dev',
        vaultCredentialId: "${SECRET_ID}",
    ]

    def secretValues = ENV_VARS.collect { value ->
        [ envVar: value, vaultKey: value ]
    }

    def secrets = [
        [
            path: "env/${PROJECT}/${APP}/.env",
            secretValues: secretValues
        ],
        [
            path: "env/${PROJECT}/${APP}/.env.${ZONE}",
            secretValues: secretValues
        ],
    ]

    withVault([
        configuration: configuration,
        vaultSecrets: secrets
    ]) {
        echo "$DB_HOST"
        echo "$DB_NAME"
    }
}
