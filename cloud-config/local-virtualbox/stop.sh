#!/usr/bin/env bash

vmName="aggregate-cloud"

# Reset the VM
vboxmanage controlvm ${vmName} poweroff
vboxmanage unregistervm ${vmName} --delete
