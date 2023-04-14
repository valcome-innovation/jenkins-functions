def call(message) {
    sh """
    curl \
    -H 'Content-Type: application/json; charset=UTF-8' \
    --data '{\"text\": \"${message}\"}' \
    -X POST 'https://chat.googleapis.com/v1/spaces/AAAA5SJWKzg/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=bklnITzE5OvP-0UHsXCTLsEbG98PNa3SsHpmUkhJTOs%3D'
    """
}