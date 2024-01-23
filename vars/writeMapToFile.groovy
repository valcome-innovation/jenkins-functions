def call(map, file) {
    def envLines = map.collect { key, value -> "$key=$value" }
    def envContent = envLines.join('\n')

    writeFile text: envContent, file: file
}