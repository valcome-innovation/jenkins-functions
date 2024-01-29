def call(map, filename) {
    def envLines = map.collect { key, value -> "$key=$value" }
    def envContent = envLines.join('\n')

    writeFile text: "${envContent}\n", file: filename
}