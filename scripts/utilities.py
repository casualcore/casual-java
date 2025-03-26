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


def clean_message(msg):
    """ removes any lines with
        Co - authored - by or
        Approved - by
    """
    lines = msg.splitlines()
    clean_lines = [line for line in lines if not (line.startswith('Co-authored-by') or line.startswith('Approved-by'))]
    return '\n'.join(clean_lines)


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


def create_expected_new_changelog_entry(version, title, body, commit_date):
    title = replace_issue_numbers(title)
    # body might not exist - if so, use empty body
    # it SHOULD exist but alas
    if body is None:
        body = ""
    body = replace_issue_numbers(body)
    version_section = f"## [{version}] - {commit_date}"
    new_entry = clean_message(f"### {title}\n{body}\n")
    return f"{version_section}\n{new_entry}"


def update_changelog(version, title, body, commit_date, changelog):
    """
    returns an updated version of the changelog
    :param version: such as 1.0.0
    :param title: The title
    :param body: The body
    :param commit_date: The commit date
    :param changelog: The changelog
    :return the updated version of the changelog:
    """
    new_entry = create_expected_new_changelog_entry(version, title, body, commit_date)
    version_section = f"## [{version}] - {commit_date}"
    if version_section not in changelog:
        changelog = changelog.replace(
            "This is the changelog for *casual java* and all changes are listed in this document.\n",
            f"This is the changelog for *casual java* and all changes are listed in this document.\n\n{version_section}\n",
            1)
    print(f"Changelog will be updated for version {version} with:\n{new_entry}")
    return changelog.replace(version_section, new_entry, 1)
