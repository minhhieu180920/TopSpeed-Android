# Create fallback icon PNGs
$dpi = @{
    'mdpi' = 48
    'hdpi' = 72
    'xhdpi' = 96
    'xxhdpi' = 144
    'xxxhdpi' = 192
}

# Simple 1x1 orange PNG (base64)
$pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAASklEQVR4nO3PMQ0AAAgDoC1U+pIcPBrwBAAAAAAAAAAAAAAAAAAAAIB/tTkAAAAAAAAAAAAAAAAAAAAAAP4LrA0lQAEK1HIAAAAASUVORK5CYII="

foreach ($density in $dpi.Keys) {
    $path = "C:\Users\MINH HIEU\Downloads\lap trinh abk\TopSpeed-Android\app\src\main\res\mipmap-$density"
    New-Item -ItemType Directory -Force -Path $path | Out-Null

    $pngBytes = [Convert]::FromBase64String($pngBase64)
    [System.IO.File]::WriteAllBytes("$path\ic_launcher.png", $pngBytes)
    [System.IO.File]::WriteAllBytes("$path\ic_launcher_round.png", $pngBytes)

    Write-Host "Created icons for $density"
}

Write-Host "All fallback icons created!"
