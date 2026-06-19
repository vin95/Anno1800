$dir = 'src\pictures\factories'
Get-ChildItem -Path $dir -File -Filter '*.png' | Where-Object { $_.Name -match 'coffee_roast?ers|coffee_rosters' } | ForEach-Object {
    $new = $_.Name -replace 'coffee_roast?ers|coffee_rosters','coffee_roaster'
    if ($new -ne $_.Name) {
        Rename-Item -Path $_.FullName -NewName $new -Force
        Write-Output "Renamed $($_.Name) -> $new"
    }
}
