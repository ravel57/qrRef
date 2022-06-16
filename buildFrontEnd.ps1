$Path = Get-Location | Select -expand Path
Set-Location "..\..\WebstormProjects\qrref-front"

yarn install
yarn build

if (Test-Path ".\dist\" -PathType Container) {
    Get-ChildItem "$Path\src\main\webapp" -Exclude "WEB-INF" | Remove-Item -Recurse
    Copy-Item -Path ".\dist\*" -Destination "$Path\src\main\webapp\" -Recurse -Force -Exclude "index.html"

    Set-Location  "$Path\src\main\webapp\js"
    Dir "app.*.js" | rename-item -newname "app.js"
    Dir "chunk-vendors.*.js" | rename-item -newname "chunk-vendors.js"

    Set-Location  "$Path\src\main\webapp\css"
    Dir app.*.css | rename-item -newname "app.css"
}
else {
    Write-Host
    Write-Host Building error -ForegroundColor Red
}