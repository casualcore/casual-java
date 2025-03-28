#-*- coding: utf-8-unix -*-

import os
from unittest import mock

from utilities import replace_issue_numbers, run_subprocess


def write_header(f):
    f.write("# Changelog\nThis is the changelog for *casual java* and all changes are listed in this document.\n\n\n")

@mock.patch.dict(os.environ, {'GITHUB_SERVER_URL': 'https://github.com', 'GITHUB_REPOSITORY': 'casualcore/casual-java'})
def create_initial_changelog():
    filename = "CHANGELOG.md"
    tags = run_subprocess('git tag --sort=-version:refname')
    with open(filename, 'w') as f:
        write_header(f)
        for tag in tags.split("\n"):
            commit_hash = run_subprocess(f'git rev-list -n 1 {tag}')
            title = run_subprocess(f'git log -1 --pretty=format:"%s" {commit_hash}')
            body = run_subprocess(f'git log -1 --pretty=format:"%b" {commit_hash}')
            commit_date = run_subprocess(f'git log -1 --pretty=format:"%cd" --date=short {commit_hash}')

            title = replace_issue_numbers(title)
            body = f"{replace_issue_numbers(body)}\n\n"

            header = f"## [{tag}] - {commit_date}\n"
            title = f"### {title}\n\n"

            f.write(header)
            f.write(title)
            if body:
                f.write(body)


create_initial_changelog()
