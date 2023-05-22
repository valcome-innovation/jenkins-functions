
def call(name,
         title,
         status, // NONE, QUEUED, IN_PROGRESS, COMPLETED
         conclusion, // NONE, ACTION_REQUIRED, SKIPPED, CANCELED, TIME_OUT, FAILURE, NEUTRAL, SUCCESS
         summary,
         text,
         detailsURL) {
    publishChecks name      : name,
                  text      : text,
                  title     : title,
                  status    : status,
                  summary   : summary,
                  conclusion: conclusion,
                  detailsURL: detailsURL
}
