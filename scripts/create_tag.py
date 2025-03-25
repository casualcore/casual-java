#-*- coding: utf-8-unix -*-
import subprocess

from utilities import get_version_from_gradle

# get the current version
version = get_version_from_gradle()
tag_name = f"{version}"  # e.g., 1.0.0
tag_message = "This is version {0}".format(tag_name)

# create a Git tag for the release
subprocess.run(['git', 'tag', '-a', tag_name, '-m', tag_message], check=True)
print(f"Created annotated tag: {tag_name}")
