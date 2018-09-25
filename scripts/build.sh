#! /bin/bash

set -e

function build_jar() {
    pushd $1
    lein install
    lein uberjar
    popd
}

build_jar core
build_jar http
build_jar utils
build_jar extra_components



