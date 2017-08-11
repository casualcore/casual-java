#!/usr/bin/python3
# -*- coding: utf-8 -*- 

import subprocess
import argparse
import sys
import json

def makeRestCall(args):
    try:
        curlExec = "curl -v --user {0}:{1} -H X-Requested-By:MyClient -H Accept:application/json -H Content-Type:multipart/form-data -F \"deployment=@{2}\" -X POST http://{3}:{4}/management/wls/latest/deployments/application/id/{5}/redeploy".format(args.user, args.passwd, args.file_name, args.host, args.port, args.application_name)
        print(curlExec)
        result = json.loads(str(subprocess.check_output(curlExec, shell = True), 'utf-8'))
        print ("\nresult: \n", result)
        if result['messages'][0]['severity'] == "SUCCESS":            
            print ("\nall is well in the kingdom of swell: ", result)
            return 0
        else:
            print ("redeploy failed!\n", result)
            return -1
    except subprocess.CalledProcessError as e:
        print >>sys.stderr, "\nExecution failed:", e
        print >>sys.stderr, "\nChild was terminated by signal", -(e.returncode)
        print >>sys.stderr, "\n:", e.output
        return -(e.returncode)

parser = argparse.ArgumentParser(description='get args')
parser.add_argument('-u', dest='user', type=str,
                   help='username')
parser.add_argument('-p', dest='passwd', type=str,
                   help='password')
parser.add_argument('-port', dest='port', type=str,
                   help='port number')
parser.add_argument('-host', dest='host', type=str,
                   help='hostname')
parser.add_argument('-name', dest='application_name', type=str,
                   help='application name')
parser.add_argument('-f', dest='file_name', type=str,
                   help='relative filename')
args = parser.parse_args()

if  args.user and args.passwd and args.port and args.host and args.application_name and args.file_name:
    sys.exit(makeRestCall(args))
else:
    parser.print_help()
    print ("Example: redeploy.py -user admin -passwd foo -host localhost -port 7001 -name my-app -f git/myapp/build/libs/myapp.ear\n")
    print ("got: \n", args)


    

