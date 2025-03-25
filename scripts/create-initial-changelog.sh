#-*- coding: utf-8-unix -*-
#!/usr/bin/env bash

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
    
    # Get the full commit message (body)
    body=$(git log -1 --pretty=format:"%b" "$commit_hash")
    
    # Get the commit date in YYYY-MM-DD format
    commit_date=$(git log -1 --pretty=format:"%cd" --date=short "$commit_hash")
    
    # Write the tag and commit details to the changelog
    echo "## [$tag] - $commit_date" >> $filename
    echo -e "- $title\n" >> $filename    

    
    
    [ -n "$body" ] && echo "$body" >> $filename
    echo "" >> $filename
done

dos2unix $filename

echo "Changelog generated successfully at CHANGELOG.md"
