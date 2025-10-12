# Setting up MyBatis Migrations on macOS & Linux 🐧

This guide will walk you through installing the MyBatis Migrations command-line tool and configuring it to work with this project on a Unix-based system like macOS or Linux.

---

### 1. Download the Tool

You can download the tool from your browser or directly from the terminal.

-   **Browser**: Go to the **[MyBatis Migrations Releases Page](https://github.com/mybatis/migrations/releases)** and download the file ending in **`-bundle.zip`** (e.g., `mybatis-migrations-3.4.0-bundle.zip`).
-   **Terminal (Recommended)**: Use `curl` to download it to your current directory.
    ```sh
    # Replace the version number with the latest if needed
    curl -LO [https://github.com/mybatis/migrations/releases/download/mybatis-migrations-3.4.0/mybatis-migrations-3.4.0-bundle.zip](https://github.com/mybatis/migrations/releases/download/mybatis-migrations-3.4.0/mybatis-migrations-3.4.0-bundle.zip)
    ```

---

### 2. Extract the Files

You need to place the tool's files in a permanent location in your home directory.

1.  Create a `tools` directory in your home folder if you don't already have one.
    ```sh
    mkdir -p ~/tools
    ```
2.  Unzip the downloaded file into that new folder.
    ```sh
    unzip mybatis-migrations-*-bundle.zip -d ~/tools
    ```
3.  You should now have a directory structure like `~/tools/mybatis-migrations-3.4.0/`.

---

### 3. Add the Tool to your `PATH`

This step allows you to run the `migrate` command from any folder in your terminal.

1.  Open your shell's configuration file in a text editor. On modern macOS, the default is Zsh (`.zshrc`). On many Linux systems, it's Bash (`.bash_profile` or `.bashrc`).
    ```sh
    # For Zsh (default on recent macOS)
    nano ~/.zshrc

    # For Bash
    # nano ~/.bash_profile
    ```
2.  Add the following line to the **end of the file**. This tells your shell where to find the `migrate` command. (Remember to replace the version number if you downloaded a different one).
    ```sh
    export PATH="$PATH:$HOME/tools/mybatis-migrations-3.4.0/bin"
    ```
3.  Save the file and exit the editor (in `nano`, press `Ctrl+X`, then `Y`, then `Enter`).

---

### 4. Verify the Installation

To apply the changes, you must reload your shell's configuration.

1.  **Completely close and reopen** your terminal window. (Alternatively, you can run `source ~/.zshrc` or `source ~/.bash_profile`).
2.  In the new terminal window, run the following command:
    ```sh
    migrate status
    ```
3.  If the installation was successful, you'll see a MyBatis Migrations message. If you see `"command not found"`, please double-check your `PATH` from Step 3.

---

### 5. Configure and Use the Project Script

Our project uses a script to manage database credentials.

#### One-Time Setup

You need to make the project's setup script executable. You only need to do this once.

1.  Navigate to the project directory.
2.  Run the `chmod` command:
    ```sh
    chmod +x setup-env.sh
    ```

#### Daily Workflow

Whenever you open a new terminal to work on the project:

1.  Navigate to the project folder.
    ```sh
    cd ~/Path/To/Your/Project/JavaCommunitySite
    ```
2.  Run the setup script to load your `.env` variables. **Remember the `.` at the beginning.**
    ```sh
    . ./setup-env.sh
    ```
3.  You are now ready to run migration commands.
    ```sh
    # Example: Check the database status
    migrate status --path=./migrations
    ```