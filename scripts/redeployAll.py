#!/usr/bin/python3
# -*- coding: utf-8 -*-

# note:
# this script assumes that RA and test application are already deployed
# it also assumes that wls is running
# This since the RA needs to have a lower deployement order than the application
# so that it starts before the application does
# Unfortunately we need to restart the wls as well 

import subprocess
import sys
import shlex
import os
from pathlib import Path

def getPath(p):
    return os.path.abspath(os.path.normpath(os.path.expanduser(p)))

def runCmd(cmdfn):
    completedProcess = subprocess.run(shlex.split(cmdfn))
    return completedProcess.returncode

def restartWLS():
    if 0 == runCmd(killWLS):
        print("\nWLS killed\n")
        if 0 == runCmd(startWLS):
            print("\n WLS started - have fun\n")
        else:
            print("failed starting the wls - you need to manually check what is going on\n")
    else:
        print("\nfailed killing the wls - you need to manually check what is going on\n")
    
rarJar = getPath('~/git/casual-java/casual/casual-jca/build/libs/casual-jca-0.0.1.rar')
testJar = getPath('~/git/casual-java-testapp/build/libs/casual-java-testapp.war')
changeDepOrder = getPath('~/bin/changeDeploymentOrder.sh')
killWLS = getPath('~/bin/killWLS.sh')
startWLS = getPath('~/bin/startWLS.sh')

print ('rarJar: {0}', rarJar)
print ('testJar: {0}', testJar)
print ('depOrder: {0}', changeDepOrder)
print ('killWLS: {0}', killWLS)
print ('startWLS: {0}', startWLS)

# redeploy RA
# run script to change deployement order for RA
# redeploy test application
# restart the wls - needed since classes from the RA are provided from the wls classpath
if 0 == runCmd('redeploy.py -u weblogic -p weblogic1 -host localhost -port 7001 -name casual-jca-0.0.1 -f {0}'.format(rarJar)):
    if 0 == runCmd(changeDepOrder):
        if 0 == runCmd('redeploy.py -u weblogic -p weblogic1 -host localhost -port 7001 -name casual-java-testapp -f {0}'.format(testJar)):
            print ("\nredeployment complete!")
            restartWLS()
        else:
            print ("\nfailed redeploying casual-java-testapp")
    else:
        print ("\nfailed chaning RA deployment order")
else:
    print ("\nfailed redeploying casual-jca-0.0.1")








