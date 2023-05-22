def call(name, title, summary, text, status, detailsURL) {
    publishChecks name      : name,
                  text      : text,
                  title     : title,
                  status    : status,
                  summary   : summary,
                  detailsURL: detailsURL
}
