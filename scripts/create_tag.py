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

# Get the current version
version = get_version_from_gradle()
tag_name = f"{version}"  # e.g., v1.0.0
tag_message = "This is version {0}".format(tag_name)

# Create a Git tag for the release
subprocess.run(['git', 'tag', '-a', tag_name, '-m', tag_message], check=True)
print(f"Created annotated tag: {tag_name}")
