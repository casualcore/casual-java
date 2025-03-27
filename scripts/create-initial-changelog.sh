#-*- coding: utf-8-unix -*-
#!/usr/bin/env bash

# helper functions
replace_issue_numbers() {
    local input="$1"
    echo "$input" | sed -E 's/#([0-9]+)/https:\/\/github.com\/casualcore\/casual-java\/issues\/\1/g'
}

# Initialize the changelog file
filename='CHANGELOG.md'

echo "# Changelog" > $filename
echo "This is the changelog for *casual java* and all changes are listed in this document." >> $filename
echo "" >> $filename

# Loop through all tags, sorted by version number (newest first)
for tag in $(git tag --sort=-version:refname); do
    # Get the commit hash for the tag
    commit_hash=$(git rev-list -n 1 "$tag")
    
    # Get the commit message (summary)
    title=$(git log -1 --pretty=format:"%s" "$commit_hash")
    title=$(replace_issue_numbers "$title")
        
    # Get the full commit message (body)
    body=$(git log -1 --pretty=format:"%b" "$commit_hash")
    body=$(replace_issue_numbers "$body")
    
    # Get the commit date in YYYY-MM-DD format
    commit_date=$(git log -1 --pretty=format:"%cd" --date=short "$commit_hash")
    
    # Write the tag and commit details to the changelog
    echo "## [$tag] - $commit_date" >> $filename
    echo -e "### $title\n" >> $filename    
      
    [ -n "$body" ] && echo "$body" >> $filename
    echo "" >> $filename
done

dos2unix $filename

echo "Changelog generated successfully at CHANGELOG.md"
