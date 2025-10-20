**BEFORE READING THIS MAKE SURE YOU HAVE COMPLETED -SETUP.GUIDE.MD**

**ENSURE THAT YOU HAVE RAN setup-env.ps1 (if windows) OR setup.env.sh (if macOS/Linux) BEFORE RUNNING SCRIPTS**

1.  Follow the MyBatis migration naming format (create .sql file):
    ```
    <version_number>_<description>.sql
    ```
    <version_number> = a numeric timestamp that acts as the unique ID for the migration.  
    - Format: `YYYYMMDDHHMMSS` (Year/Month/Day/Hour/Minute/Second)  
    - Example: `20251020113312`  
    - **Purpose:** MyBatis uses this number as the migration's primary identifier in the database.  
    - **Important:** Each migration must have a **unique and sequential** `version_number`.  
    - The number must increase over time compared to previously applied migrations.  
    - This ensures migrations are applied in the correct order and prevents conflicts.
    <description> = short text describing the migration, e.g., add_is_deleted_to_post
    For example:
    ```
        20251020113312_add_is_delete_to_post.sql
    ```
2.  Run Migration commands: 
    # Check current database migration status
    migrate status --path=./migrations

    # Apply all pending migrations (up)
    migrate up --path=./migrations

    # Roll back the most recent migration (down)
    migrate down --path=./migrations
3. Verify changes:
    After migrate up, check the database to confirm that new columns, tables, or changes were applied successfully.

    After migrate down, ensure that the last migration changes were reverted.