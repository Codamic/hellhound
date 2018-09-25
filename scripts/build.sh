#! /bin/bash

set -e
lein_path=$1

function build_jar() {
    pushd $1
    $lein_path/lein install
    $lein_path/lein uberjar
    popd
}

build_jar core
build_jar http
build_jar utils
build_jar extra_components

$lein_path/lein deps
$lein_path/lein install
$lein_path/lein ubderjar
