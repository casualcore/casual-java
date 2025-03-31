#-*- coding: utf-8-unix -*-
import os
import sys
from utilities import validate_format

pr_title = os.getenv("PR_TITLE", "").strip()
is_valid = validate_format(pr_title)
if not is_valid:
    sys.exit(1)  # exit with error code to fail the workflow
