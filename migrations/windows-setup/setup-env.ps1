# This script finds and loads the .env file for the project.
Write-Host "Loading project environment variables..."

# Start in the current directory and search upwards for the .env file
$currentDir = Get-Location
$envFile = Join-Path $currentDir.Path ".env"

while (-not (Test-Path $envFile) -and $currentDir.Path -ne $currentDir.Root) {
    $currentDir = Split-Path $currentDir.Path -Parent
    $envFile = Join-Path $currentDir.Path ".env"
}

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^(\w+)=(.*)') {
            Set-Item -Path "Env:\$($Matches[1])" -Value $Matches[2]
        }
    }
    Write-Host "✅ Variables loaded from: $envFile"
    Write-Host "You can now run your project commands."
} else {
    Write-Host "❌ No .env file found in this or any parent directory."
}