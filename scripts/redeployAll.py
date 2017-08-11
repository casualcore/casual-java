#!/usr/bin/python3
# -*- coding: utf-8 -*-

import subprocess
import sys
import shlex
import os
from pathlib import Path

def getPath(p):
    return os.path.abspath(os.path.normpath(os.path.expanduser(p)))

rarJar = getPath('~/git/casual-java/casual/casual-jca/build/libs/casual-jca-0.0.1.rar')
testJar = getPath('~/git/casual-java-testapp/build/libs/casual-java-testapp.war')

print ('rarJar: {0}', rarJar)
print ('testJar: {0}', testJar)

def redeploy(cmdfn):
    completedProcess = subprocess.run(shlex.split(cmdfn))
    return completedProcess.returncode

if 0 == redeploy('redeploy.py -u weblogic -p weblogic1 -host localhost -port 7001 -name casual-jca-0.0.1 -f {0}'.format(rarJar)):
    if 0 == redeploy('redeploy.py -u weblogic -p weblogic1 -host localhost -port 7001 -name casual-java-testapp -f {0}'.format(testJar)):
        print ("\nredeployment complete!")
    else:
        print ("\nfailed redeploying casual-java-testapp")
else:
    print ("\nfailed redeploying casual-jca-0.0.1")




