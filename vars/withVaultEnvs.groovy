def call(SECRET_ID,
         PROJECT,
         APP,
         ZONE) {

    def configuration = [
        vaultUrl: 'https://vault.valcome.dev',
        vaultCredentialId: "${SECRET_ID}",
    ]

    def secrets = [
        [
            path: "env/${PROJECT}/${APP}/.env",
            secretValues: [
                [ envVar: 'DB_HOST', vaultKey: 'DB_HOST' ],
                [ envVar: 'DB_NAME', vaultKey: 'DB_NAME' ]
            ]
        ],
        [
            path: "env/${PROJECT}/${APP}/.env.${ZONE}",
            secretValues: [
                [ envVar: 'DB_HOST', vaultKey: 'DB_HOST' ],
                [ envVar: 'DB_NAME', vaultKey: 'DB_NAME' ]
            ]
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
