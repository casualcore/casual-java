#-*- coding: utf-8-unix -*-
import subprocess
from datetime import datetime
from utilities import get_version_from_gradle, replace_issue_numbers, clean_message

# Get the current version
version = get_version_from_gradle()

# Get the latest commit message
commit_msg = subprocess.getoutput("git log -1 --pretty=%B").strip().replace("\\r\\n", '\n')
commit_msg = replace_issue_numbers(commit_msg)

# Get the commit date in YYYY-MM-DD format
commit_date_raw = subprocess.getoutput("git log -1 --pretty=%cd --date=short")
commit_date = datetime.strptime(commit_date_raw, "%Y-%m-%d").strftime("%Y-%m-%d")

# Read the current changelog (or initialize it if it doesn’t exist)
try:
    with open('CHANGELOG.md', 'r', encoding='utf-8') as f:
        changelog = f.read()
except FileNotFoundError:
    changelog = "# Changelog\n"

# Find or create the version section
version_section = f"## [{version}] - {commit_date}"
if version_section not in changelog:
    changelog = changelog.replace("This is the changelog for *casual java* and all changes are listed in this document.\n",
                                  f"This is the changelog for *casual java* and all changes are listed in this document.\n\n{version_section}\n", 1)

# Append the commit message under the version section
new_entry = clean_message(f"- {commit_msg}\n")
changelog = changelog.replace(version_section, f"{version_section}\n{new_entry}", 1)

# Write the updated changelog back to the file
with open('CHANGELOG.md', 'w', encoding='utf-8') as f:
    f.write(changelog)

print(f"Changelog updated for version {version} with commit: {commit_msg}")
