#-*- coding: utf-8-unix -*-
import unittest
import re

from utilities import get_version_from_gradle, replace_issue_numbers, validate_format


def create_issue_replacement(issue_number, base_url):
    return f"[#{issue_number}]({base_url}/{issue_number})"


class UtilitiesTest(unittest.TestCase):
    @staticmethod
    def test_version_matches():
        version = get_version_from_gradle().strip()
        assert re.match("^[A-Za-z0-9]*", version)

    def test_issue_replace_nothing(self):
        msg = 'This is a message that contains no issues at all'
        replaced_msg = replace_issue_numbers(msg)
        self.assertEqual(msg, replaced_msg, 'no issues, msg should be unchanged')

    def test_one_issue_is_replaced(self):
        base_msg = 'This is the message '
        issue_number = '143'
        msg = f"{base_msg} - #{issue_number}"
        base_url = 'https://github.com/casualcore/casual-java/issues'
        issue_replacement = create_issue_replacement(issue_number, base_url)
        expected = f"{base_msg} - {issue_replacement}"
        actual = replace_issue_numbers(msg)
        self.assertEqual(expected, actual, f"expected: {expected} should equal actual {actual}")

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


if __name__ == '__main__':
    unittest.main()
