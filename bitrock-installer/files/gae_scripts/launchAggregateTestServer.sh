#!/bin/bash
# Launches the development AppServer
[ -z "${DEBUG}" ] || set -x  # trace if $DEBUG env. var. is non-zero
UPLOAD_ROOT=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
@$UPLOAD_ROOT/bin/dev_appserver.sh ODKAggregate
