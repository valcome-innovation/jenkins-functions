def call(project, branch, host) {
    build job: '/ng-live/init-db/dev/dev-db',
          parameters: [
              string(name: 'INIT_COMMAND', value: "db:${project}:init:dev"),
              string(name: 'BRANCH', value: branch),
              string(name: 'HOST', value: host)
          ]
}
