#!/bin/bash
# This script finds and loads the .env file by searching upwards from the current directory.

# Start in the current directory
current_dir=$(pwd)

# Loop upwards until we find a .env file or hit the root directory
while [ "$current_dir" != "/" ]; do
    if [ -f "$current_dir/.env" ]; then
        echo "✅ Variables loaded from: $current_dir/.env"
        # Read each line, trim whitespace, and ignore comments/empty lines
        while IFS= read -r line || [ -n "$line" ]; do
            if [[ ! "$line" =~ ^\s*# ]] && [[ "$line" =~ = ]]; then
                export "$line"
            fi
        done < "$current_dir/.env"
        echo "You can now run your project commands."
        # Exit the script successfully
        return 0
    fi
    # Move to the parent directory
    current_dir=$(dirname "$current_dir")
done

# If the loop finishes, no .env file was found
echo "❌ No .env file found in this or any parent directory."