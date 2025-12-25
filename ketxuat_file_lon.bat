# Tìm file > 1GB trong thư mục hiện tại
Get-ChildItem -Recurse | Where-Object {$_.Length -gt 1GB} | Sort-Object Length -Descending

# Tìm file > 500MB trong ổ C:
Get-ChildItem -Path "C:\" -Recurse -File -ErrorAction SilentlyContinue | Where-Object {$_.Length -gt 500MB} | Select-Object FullName, @{Name="SizeGB";Expression={[math]::Round($_.Length/1GB,2)}} | Sort-Object SizeGB -Descending

# Export kết quả ra CSV
Get-ChildItem -Path "C:\" -Recurse -File -ErrorAction SilentlyContinue | Where-Object {$_.Length -gt 100MB} | Select-Object FullName, Length, LastWriteTime | Export-Csv "large_files.csv"