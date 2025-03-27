#-*- coding: utf-8-unix -*-
import os
from unittest import TestCase, mock, main

from utilities import replace_issue_numbers, get_url
from utils_for_test import create_issue_replacement, github_server_url, github_repository


class PreviewChangelog(TestCase):
    TITLE_NO_ISSUE = "feat: No issue"
    BODY_NO_ISSUE = "No issue!"

    FIRST_ISSUE = '143'
    BODY_ISSUE = f"Has at least one issue "

    PR_TITLE = "PR_TITLE"
    PR_BODY = "PR_BODY"

    @mock.patch.dict(os.environ, {'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}",
                                  f"{PR_TITLE}": TITLE_NO_ISSUE, f"{PR_BODY}": BODY_NO_ISSUE})
    def test_no_issue(self):
        self.assertEqual(os.getenv(self.PR_TITLE), self.TITLE_NO_ISSUE)
        self.assertEqual(os.getenv(self.PR_BODY), self.BODY_NO_ISSUE)

        commit_msg = f"{os.getenv(self.PR_TITLE)}\n{os.getenv(self.PR_BODY)}"
        actual = replace_issue_numbers(commit_msg)
        self.assertEqual(commit_msg, actual)

    @mock.patch.dict(os.environ, {'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}",
                                  f"{PR_TITLE}": TITLE_NO_ISSUE, f"{PR_BODY}": BODY_ISSUE})
    def test_one_issue(self):
        self.assertEqual(os.getenv(self.PR_TITLE), self.TITLE_NO_ISSUE)
        self.assertEqual(os.getenv(self.PR_BODY), self.BODY_ISSUE)

        body_with_issue = f"{os.getenv(self.PR_BODY)}#{self.FIRST_ISSUE}"
        commit_msg = f"{os.getenv(self.PR_TITLE)}\n{body_with_issue}"
        actual = replace_issue_numbers(commit_msg)
        issue_replacement = create_issue_replacement(self.FIRST_ISSUE, get_url())
        body_with_replaced_issue = f"{os.getenv(self.PR_BODY)}{issue_replacement}"
        expected = f"{os.getenv(self.PR_TITLE)}\n{body_with_replaced_issue}"
        self.assertEqual(actual, expected)


if __name__ == '__main__':
    main()
