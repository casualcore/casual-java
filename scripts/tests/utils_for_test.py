#-*- coding: utf-8-unix -*-


def create_issue_replacement(issue_number, base_url):
    return f"[#{issue_number}]({base_url}/{issue_number})"

def github_server_url():
    return 'https://github.com'

def github_repository():
    return 'casualcore/casual-java'
