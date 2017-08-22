#!/bin/sh
kill -9 $(ps -ef | grep -i weblogic | awk -F' ' '{print $2}' | xargs | awk -F' ' '{$NF=""; print $0}')
