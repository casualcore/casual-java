#-*- coding: utf-8-unix -*-
import os
from datetime import date

from utilities import get_version_from_gradle, clean_message, replace_issue_numbers
version = get_version_from_gradle()

pr_title = os.getenv("PR_TITLE", "").strip()
pr_body = os.getenv("PR_BODY", "").strip()

commit_msg = replace_issue_numbers(f"### {pr_title}\n{pr_body}")
today = date.today()
title = f"## [{version}] - {today}"

new_entry = clean_message(f"{commit_msg}\n")

print(f"{title}\n{new_entry}")
