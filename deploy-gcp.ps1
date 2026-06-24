# GCP deploy script: build + upload
# Set the two variables below before use
$VM_NAME = "instance-free-e2-micro"
$ZONE    = "us-west1-b"

# -- Get git commit hash (short, 7 chars)
$GIT_HASH = git rev-parse --short HEAD
if (-not $?) {
    Write-Host "Failed to get git commit hash. Make sure git is installed and you are in a git repo." -ForegroundColor Red
    exit 1
}
Write-Host "[*] Git commit: $GIT_HASH" -ForegroundColor DarkGray

# -- Step 1: Maven clean package
Write-Host "[1] Maven clean package (version=$GIT_HASH)" -ForegroundColor Cyan
& "$PSScriptRoot\mvnw.cmd" clean package -DskipTests "-Drevision=$GIT_HASH"
if (-not $?) {
    Write-Host "BUILD FAILED" -ForegroundColor Red
    exit 1
}

# -- Locate jar (name contains hash, exclude .original)
$JAR = Get-ChildItem "$PSScriptRoot\target\rungame-*.jar" |
       Where-Object { $_.Name -notlike "*.original" } |
       Select-Object -First 1

if (-not $JAR) {
    Write-Host "Jar not found. Check the target/ directory." -ForegroundColor Red
    exit 1
}
Write-Host "[*] Found jar: $($JAR.Name)" -ForegroundColor DarkGray

# -- Step 2: gcloud scp upload jar (relative remote path = home dir; pscp does not expand ~)
Write-Host "[2] Uploading to ${VM_NAME}:rungame.jar (home dir)" -ForegroundColor Cyan
gcloud compute scp $JAR.FullName "${VM_NAME}:rungame.jar" --zone=$ZONE
if (-not $?) {
    Write-Host "UPLOAD FAILED" -ForegroundColor Red
    exit 1
}

# -- Step 3: SSH into VM and restart the rungame service
Write-Host "[3] Restarting rungame service on VM" -ForegroundColor Cyan
gcloud compute ssh $VM_NAME --zone=$ZONE --command="sudo systemctl restart rungame && sudo systemctl status rungame --no-pager -n 5"
if (-not $?) {
    Write-Host "RESTART FAILED" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Done.  jar=$($JAR.Name)  -> rungame.jar  (service restarted)" -ForegroundColor Green
