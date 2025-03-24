#-*- coding: utf-8-unix -*-
import re
import subprocess
from datetime import datetime

# Function to extract version from versions.gradle
def get_version_from_gradle():
    with open("versions.gradle", "r") as f:
        for line in f:
            if "version" in line:
                match = re.search(r"version\s*=\s*['\"]([^'\"]+)['\"]", line)
                if match:
                    return match.group(1)
    raise ValueError("Version not found in versions.gradle")

# Function to replace issue numbers with markdown link
# such as:
# #132 -> [#132](https://github.com/casualcore/casual-java/issues/132)
def replace_issue_numbers(commit_msg):
    pattern = r'#(\d+)' 
    def replacement(match):
        number = match.group(1)  
        return f'[{match.group(0)}](https://github.com/casualcore/casual-java/issues/{number})'
    return re.sub(pattern, replacement, commit_msg)

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
    with open("CHANGELOG.md", "r") as f:
        changelog = f.read()
except FileNotFoundError:
    changelog = "# Changelog\n"

# Find or create the version section
version_section = f"## [{version}] - {commit_date}"
if version_section not in changelog:
    changelog = changelog.replace("This is the changelog for *casual java* and all changes are listed in this document.\n", f"This is the changelog for *casual java* and all changes are listed in this document.\n\n{version_section}\n", 1)

# Append the commit message under the version section
new_entry = f"- {commit_msg}\n"
changelog = changelog.replace(version_section, f"{version_section}\n{new_entry}", 1)

# Write the updated changelog back to the file
with open("CHANGELOG.md", "w") as f:
    f.write(changelog)

print(f"Changelog updated for version {version} with commit: {commit_msg}")
