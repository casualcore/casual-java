#-*- coding: utf-8-unix -*-
import os
import sys
from utilities import validate_format

# Get the commit message from the environment variable set by GitHub Actions
commit_msg = os.getenv("COMMIT_MESSAGE", "").strip()
is_valid = validate_format(commit_msg)
if not is_valid:
    sys.exit(1)  # exit with error code to fail the workflow
