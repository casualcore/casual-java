#-*- coding: utf-8-unix -*-
import re
import os
import sys

# Get the commit message from the environment variable set by GitHub Actions
commit_msg = os.getenv("COMMIT_MESSAGE", "").strip()

# Define the conventional commit pattern
pattern = r"^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+"

# Validate the commit message
if not re.match(pattern, commit_msg):
    print(f"Error: Commit message '{commit_msg}' does not follow conventional commit format.")
    print("Format: <type>[optional scope]: <description>")
    print("Example: feat: add new feature")
    print("Allowed types: feat, fix, docs, style, refactor, test, chore")
    sys.exit(1)
else:
    print(f"Commit message '{commit_msg}' is valid.")
