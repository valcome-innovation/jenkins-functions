static def call(n) {
    def alphabet = (('A'..'Z')+('0'..'9')).join();

    return new Random().with {
        (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
    }
}