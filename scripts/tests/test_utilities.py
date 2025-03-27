#-*- coding: utf-8-unix -*-
import os
import re
from unittest import TestCase, mock, main

from utilities import get_version_from_gradle, replace_issue_numbers, validate_format, clean_message, update_changelog, \
    create_expected_new_changelog_entry
from utils_for_test import create_issue_replacement, github_server_url, github_repository


class UtilitiesTest(TestCase):
    @staticmethod
    def test_version_matches():
        version = get_version_from_gradle().strip()
        assert re.match("^[A-Za-z0-9]*", version)

    @mock.patch.dict(os.environ, {'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}"})
    def test_issue_replace_nothing(self):
        msg = 'This is a message that contains no issues at all'
        replaced_msg = replace_issue_numbers(msg)
        self.assertEqual(msg, replaced_msg, 'no issues, msg should be unchanged')

    @mock.patch.dict(os.environ,{'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}"})
    def test_one_issue_is_replaced(self):
        base_msg = 'This is the message '
        issue_number = '143'
        msg = f"{base_msg} - #{issue_number}"
        base_url = 'https://github.com/casualcore/casual-java/issues'
        issue_replacement = create_issue_replacement(issue_number, base_url)
        expected = f"{base_msg} - {issue_replacement}"
        actual = replace_issue_numbers(msg)
        self.assertEqual(expected, actual, f"expected: {expected} should equal actual {actual}")

    @mock.patch.dict(os.environ,{'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}"})
    def test_two_issues_is_replaced(self):
        base_msg = 'This is the message '
        issue_number_one = '143'
        issue_number_two = '245'
        msg = f"{base_msg} - #{issue_number_one}, #{issue_number_two}"
        base_url = 'https://github.com/casualcore/casual-java/issues'
        issue_replacement_one = create_issue_replacement(issue_number_one, base_url)
        issue_replacement_two = create_issue_replacement(issue_number_two, base_url)
        expected = f"{base_msg} - {issue_replacement_one}, {issue_replacement_two}"
        actual = replace_issue_numbers(msg)
        self.assertEqual(expected, actual, f"expected: {expected} should equal actual {actual}")

    def test_valid_formats(self):
        titles = ['feat: shiny', 'fix: shiny', 'docs: shiny', 'style: shiny',
                  'refactor: shiny', 'test: shiny', 'chore: shiny']
        for title in titles:
            self.assertTrue(validate_format(title), f"{title} is not valid")

    def test_invalid_formats(self):
        titles = ['feature: shiny', 'fixt: shiny', 'docsa: shiny', 'styles: shiny',
                  'refactored: shiny', 'tested: shiny', 'chored: shiny', 'Best commit ever']
        for title in titles:
            self.assertFalse(validate_format(title), f"{title} is valid but it should be invalid")

    def test_clean_messages(self):
        message_already_clean = "This is a very nice message"
        self.assertEqual(message_already_clean, clean_message(message_already_clean))

        co_author_one = "Co-authored-by: janedoe <jdoe@gmail.com>"
        co_author_two = "Co-authored-by: johndoe <jdoe@gmail.com>"
        message_with_co_authors = f"{message_already_clean}\n{co_author_one}\n{co_author_two}"
        self.assertEqual(message_already_clean, clean_message(message_with_co_authors))

        approved_by_one = "Approved-by: janedoe <jdoe@gmail.com>"
        approved_by_two = "Approved-by: johndoe <jdoe@gmail.com>"
        message_with_co_authors = f"{message_already_clean}\n{approved_by_one}\n{approved_by_two}"
        self.assertEqual(message_already_clean, clean_message(message_with_co_authors))

    @mock.patch.dict(os.environ,
                     {'GITHUB_SERVER_URL': f"{github_server_url()}", 'GITHUB_REPOSITORY': f"{github_repository()}"})
    def test_update_changelog(self):
        version = '1.1.1'
        title = 'feat: nice feature'
        body = 'Very nice body'
        commit_date = '2025-01-04'
        changelog = "# Changelog\nThis is the changelog for *casual java* and all changes are listed in this document.\n\n## [1.1.0] - 2025-01-13\n### feat: unmarshall all null values (#143)\n* handle null values correctly\n* This fixes #131"
        changelog = update_changelog(version, title, body, commit_date, changelog)
        expected_new_entry = create_expected_new_changelog_entry(version, title, body, commit_date)
        self.assertIn(expected_new_entry, changelog, f"{expected_new_entry} missing from {changelog}")


if __name__ == '__main__':
    main()
