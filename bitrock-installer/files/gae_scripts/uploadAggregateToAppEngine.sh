#!/bin/bash
clear
MODE=update
if (( $# == 1 )); then
  MODE=$1
  echo Performing ${MODE} action
fi
echo "Please enter the email account that created or"
echo "that can update your ODK Aggregate application."
echo
read -er -p "Email account (e.g., user@gmail.com): "
EMAIL=${REPLY}
read -ers -p "Email account password: "
PASSWD=${REPLY}
echo
# zero is success...
OUTCOME=1
# Launches AppCfg
[ -z "${DEBUG}" ] || set -x  # trace if $DEBUG env. var. is non-zero
UPLOAD_ROOT=`dirname "$0" | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
cd "$UPLOAD_ROOT"
( ( ( "$UPLOAD_ROOT/appengine-java-sdk/bin/appcfg.sh" --email=${EMAIL} --passin ${MODE} ODKAggregate 2>&1 && echo '---- WEBSITE COMPLETE - BEGIN BACKEND ----' &&  "$UPLOAD_ROOT/appengine-java-sdk/bin/appcfg.sh" --email=${EMAIL} --passin backends ${MODE} ODKAggregate 2>&1 && OUTCOME=0 && echo ---END-SCRIPT_SUCCESS--- ) || echo ---END-SCRIPT-FAILURE--- ) | sed -e"/assword fo/s/.*//" ) << __THE__END__
${PASSWD}
__THE__END__
read -p "Press any key to close window . . ."
# OUTCOME=1 on failure...
# regardless, try to open the README so the user can click the link to the appspot instance.
GNPATH=`which gnome-open`
if (( ${#GNPATH}==0 )); then
# osx
nohup open "${UPLOAD_ROOT}/README.html" 2>&1 > /dev/null
else
# linux
nohup gnome-open "${UPLOAD_ROOT}/README.html" 2>&1 > /dev/null
fi
# give time for launch to happen before dropping Terminal window...
sleep 1
