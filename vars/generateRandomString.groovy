static def call(n) {
    def alphabet = (('A'..'Z') + ('0'..'9')).join();
    def rand = new Random()

    return (1..n).collect {
        alphabet[rand.nextInt(alphabet.length())]
    }.join()
}