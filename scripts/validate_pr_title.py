#-*- coding: utf-8-unix -*-
import re
import os
import sys

# Get the PR title from an environment variable
pr_title = os.getenv("PR_TITLE", "").strip()

# Define the conventional commit pattern
pattern = r"^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+"

# Validate the PR title
if not re.match(pattern, pr_title):
    print(f"Error: PR title '{pr_title}' does not follow conventional commit format.")
    print("Format: <type>[optional scope]: <description>")
    print("Example: feat: add new feature")
    print("Allowed types: feat, fix, docs, style, refactor, test, chore")
    sys.exit(1)  # Exit with error code to fail the workflow
else:
    print(f"PR title '{pr_title}' is valid.")
