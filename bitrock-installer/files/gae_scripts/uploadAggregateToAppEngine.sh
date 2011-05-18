#!/bin/bash
# Launches AppCfg
[ -z "${DEBUG}" ] || set -x  # trace if $DEBUG env. var. is non-zero
UPLOAD_ROOT=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
$UPLOAD_ROOT/appengine-sdk-java/bin/appcfg.sh --enable_jar_splitting update ODKAggregate
echo Press any key to continue . . .
read
