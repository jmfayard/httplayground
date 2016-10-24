#!/usr/bin/env bash -x
which newman || {
    echo "please install newman"
    open https://www.npmjs.com/package/newman
    exit 1
}

export REPORTERS="--reporter  cli,html,junit "
export COLLECTION="postman_httpbin.json"
export GLOBALS="-g globals.postman_globals.json"
export ENV=""
cd postman
newman run ${REPORTERS} ${GLOBALS} ${ENV} ${COLLECTION}
cd --