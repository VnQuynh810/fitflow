$repo = "wind5293/Exercise_GIFs"
$releaseUrl = "https://api.github.com/repos/$repo/releases/latest"
$repoRoot = Split-Path -Parent $PSScriptRoot
$assetsDir = Join-Path $repoRoot "app/src/main/assets/gifs"

if (-not (Test-Path -Path $assetsDir)) {
    New-Item -ItemType Directory -Path $assetsDir -Force | Out-Null
    Write-Host "Created directory: $assetsDir"
}

Write-Host "Fetching latest release from $repo..."
$release = Invoke-RestMethod -Uri $releaseUrl

if ($null -eq $release.assets -or $release.assets.Count -eq 0) {
    Write-Host "No assets found in the latest release."
    exit 0
}

Write-Host "Found $($release.assets.Count) GIFs to download into $assetsDir."
$client = New-Object System.Net.WebClient

foreach ($asset in $release.assets) {
    $fileName = $asset.name
    $destination = Join-Path $assetsDir $fileName
    if (Test-Path $destination) {
        continue
    }
    Write-Host "Downloading: $fileName..."
    try {
        $client.DownloadFile($asset.browser_download_url, $destination)
    } catch {
        Write-Warning "Failed to download $fileName : $_"
    }
}

Write-Host "All exercise GIFs are ready!"
