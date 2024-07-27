$sourceDir = "W:\Projects\Java\Drifter\src\main\java\ca\volatilecobra\terrain"
$destDir = "W:\Projects\Java\Drifter\Server\src\main\java\ca\volatilecobra\"

Remove-Item "$destDir/terrain" -Recurse -Force



# Sync files
Copy-Item -Path $sourceDir -Destination $destDir -Recurse -Force

(Get-Content "$destDir/terrain/chunk/WorldChunk.java") | ForEach-Object {
    $_ -replace "Drifter", "GameServer"

} | Set-Content "$destDir/terrain/chunk/WorldChunk.java"