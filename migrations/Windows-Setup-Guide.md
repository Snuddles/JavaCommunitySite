# Setting up MyBatis Migrations on Windows 💻

This guide will walk you through installing the MyBatis Migrations command-line tool and configuring it to work with this project.

---

### 1. Download the Tool

First, you need to download the official tool from its GitHub repository.

-   Go to the **[MyBatis Migrations Releases Page](https://github.com/mybatis/migrations/releases)**.
-   Find the latest release and download the file ending in **`-bundle.zip`** (e.g., `mybatis-migrations-3.4.0-bundle.zip`).

---

### 2. Extract the Files

You need to place the tool's files in a permanent location on your computer.

1.  Open File Explorer and navigate to your `C:` drive.
2.  Create a new folder named `tools`. The path will be `C:\tools`.
3.  Unzip the contents of the downloaded file into this new folder. You should now have a directory structure like `C:\tools\mybatis-migrations-3.4.0\`.

---

### 3. Add the Tool to the Windows `Path`

This is the most important step. It allows you to run the `migrate` command from any folder in your terminal.

1.  Press the `Windows Key`, type **`Edit the system environment variables`**, and press Enter.
2.  In the window that opens, click the **"Environment Variables..."** button.
3.  In the top box ("User variables for..."), find the **`Path`** variable, select it, and click **"Edit..."**.
4.  In the new window, click **"New"**.
5.  Paste the full path to the **`bin`** directory from Step 2. For example: `C:\tools\mybatis-migrations-3.4.0\bin`.
    
6.  Click **OK** on all the windows to close and save them.

---

### 4. Verify the Installation

To make sure the setup was successful, you need to check if Windows can find the command.

1.  **Completely close and reopen** any open PowerShell or Command Prompt windows. This is required for the `Path` changes to take effect.
2.  In the new terminal, run the following command:
    ```powershell
    migrate status
    ```
3.  If the installation was successful, you'll see a MyBatis Migrations message. If you see `"command not found"`, please double-check your `Path` variable from Step 3.

---

### 5. Configure and Use the Project Script

Our project uses a script to manage database credentials. You only need to do the setup part once.

#### One-Time Setup

If this is your first time running a local script, you may need to update PowerShell's security policy.

1.  Open PowerShell **as an Administrator**.
2.  Run this command and press `Y` to confirm:
    ```powershell
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
    ```

#### Daily Workflow

Whenever you open a new terminal to work on the project:

1.  Navigate to the project folder.
    ```powershell
    cd C:\Path\To\Your\Project\JavaCommunitySite
    ```
2.  Run the setup script to load your `.env` variables.
    ```powershell
    . .\setup-env.ps1
    ```
3.  You are now ready to run migration commands.
    ```powershell
    # Example: Check the database status
    migrate status --path=./migrations
    ```