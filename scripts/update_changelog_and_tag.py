#-*- coding: utf-8-unix -*-
import re
import subprocess

# Function to extract version from versions.gradle
def get_version_from_gradle():
    with open("versions.gradle", "r") as f:
        for line in f:
            if "version" in line:
                match = re.search(r"version\s*=\s*['\"]([^'\"]+)['\"]", line)
                if match:
                    return match.group(1)
    raise ValueError("Version not found in versions.gradle")

# Get the current version
version = get_version_from_gradle()
tag_name = f"{version}"  # e.g., v1.0.0
tag_message = "This is version {0}".format(tag_name)

# Create a Git tag for the release
subprocess.run(["git", "tag", "-a {0}".format(tag_name), "-m", tag_message], check=True)
print(f"Created tag: {tag_name}")

# Get the latest commit message
commit_msg = subprocess.getoutput("git log -1 --pretty=%B").strip()

# Read the current changelog (or initialize it if it doesn’t exist)
try:
    with open("CHANGELOG.md", "r") as f:
        changelog = f.read()
except FileNotFoundError:
    changelog = "# Changelog\n"

# Find or create the version section
version_section = f"## [{version}]"
if version_section not in changelog:
    changelog = changelog.replace("# Changelog\n", f"# Changelog\n\n{version_section}\n", 1)

# Append the commit message under the version section
new_entry = f"- {commit_msg}\n"
changelog = changelog.replace(version_section, f"{version_section}\n{new_entry}", 1)

# Write the updated changelog back to the file
with open("CHANGELOG.md", "w") as f:
    f.write(changelog)

print(f"Changelog updated for version {version} with commit: {commit_msg}")

