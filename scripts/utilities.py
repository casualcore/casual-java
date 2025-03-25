#-*- coding: utf-8-unix -*-
import re


def get_version_from_gradle():
    with open("versions.gradle", "r") as f:
        for line in f:
            if "version" in line:
                match = re.search(r"version\s*=\s*['\"]([^'\"]+)['\"]", line)
                if match:
                    return match.group(1)
    raise ValueError("Version not found in versions.gradle")


def replace_issue_numbers(commit_msg):
    """ Function to replace issue numbers with markdown link
    such as:
    #132 -> [#132](https://github.com/casualcore/casual-java/issues/132)
    """
    pattern = r'#(\d+)'

    def replacement(match):
        number = match.group(1)
        return f'[{match.group(0)}](https://github.com/casualcore/casual-java/issues/{number})'
    return re.sub(pattern, replacement, commit_msg)


def validate_format(msg):
    pattern = r"^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+"
    if not re.match(pattern, msg):
        print(f"Error: PR title '{msg}' does not follow conventional commit format.")
        print("Format: <type>[optional scope]: <description>")
        print("Example: feat: add new feature")
        print("Allowed types: feat, fix, docs, style, refactor, test, chore")
        return False
    else:
        print(f"PR title '{msg}' is valid.")
        return True
