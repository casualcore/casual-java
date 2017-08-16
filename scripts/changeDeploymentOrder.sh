#!/bin/sh
# -*- coding: utf-8 -*-
. ~/usr/local/weblogic/wlserver/server/bin/setWLSEnv.sh
java weblogic.WLST ~/bin/setDeploymentOrder.py

