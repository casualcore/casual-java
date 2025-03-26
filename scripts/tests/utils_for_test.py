#-*- coding: utf-8-unix -*-


def create_issue_replacement(issue_number, base_url):
    return f"[#{issue_number}]({base_url}/{issue_number})"
