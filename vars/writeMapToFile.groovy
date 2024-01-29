def call(map, filename) {
    def envLines = map.collect { key, value -> "$key=$value" }
    def envContent = envLines.join('\n') + '\n'

    writeFile text: envContent, file: filename
}