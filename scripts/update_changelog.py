#-*- coding: utf-8-unix -*-
import subprocess
from datetime import datetime

from utilities import get_version_from_gradle, update_changelog

version = get_version_from_gradle()

title = subprocess.getoutput("git log -1 --pretty=%s").strip().replace("\\r\\n", '\n')
body = subprocess.getoutput("git log -1 --pretty=%b").strip().replace("\\r\\n", '\n')
commit_date_raw = subprocess.getoutput("git log -1 --pretty=%cd --date=short")
commit_date = datetime.strptime(commit_date_raw, "%Y-%m-%d").strftime("%Y-%m-%d")

# read the current changelog (or initialize it if it doesn’t exist)
try:
    with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
        changelog = f.read()
except FileNotFoundError:
    changelog = "# Changelog\n"

changelog = update_changelog(version, title, body, commit_date, changelog)

# write the updated changelog back to the file
with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(changelog)

print(f"Changelog updated for version {version}")
