#!/bin/bash -ex

(
	cd "${0%/*}"
	if [ ! -d target/test-classes/javascript ]; then
		mvn dependency:unpack-dependencies@unpack-javascript
	fi
)


search=( "${0%/*}/src/main/resources/javascript" $(find "${0%/*}/target/classes/javascript" -type d) )

exec dbc-jsshell --search "${search[*]/#/file:}" "$@"
