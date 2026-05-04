# Refactoring script to move files to vertical slice architecture
$baseDir = "c:\Users\Liezel Ybanez\IT342_UniGear-Tracker_G4_Ybanez\backend\src\main\java\com\unigear\tracker"
$featuresDir = "$baseDir\features"

# Step 1: Create complete directory structure
Write-Host "=== Step 1: Creating directory structure ===" -ForegroundColor Green
$features = @("admin","equipment","profile","request","user","auth")
$subdirs = @("controller","service","repository","entity","dto")

foreach ($feat in $features) {
    foreach ($dir in $subdirs) {
        $path = "$featuresDir\$feat\$dir"
        if (-not (Test-Path $path)) {
            New-Item -ItemType Directory -Force -Path $path | Out-Null
            Write-Host "  ✓ Created $feat\$dir"
        }
    }
}

# Step 2: Copy/move all files to features
Write-Host "`n=== Step 2: Moving files to features ===" -ForegroundColor Green
cd $baseDir

# Dictionary of file moves: source -> destination
$moves = @{
    # Admin Feature
    "controller\AdminController.java" = "features\admin\controller\"
    "dto\AdminUserDto.java" = "features\admin\dto\"
    "service\AdminAccountInitializer.java" = "features\admin\service\"
    
    # Auth Feature (add remaining DTOs)
    "dto\AuthResponse.java" = "features\auth\dto\"
    "dto\LoginRequest.java" = "features\auth\dto\"
    "dto\RegisterRequest.java" = "features\auth\dto\"
    
    # Equipment Feature
    "dto\CreateEquipmentDto.java" = "features\equipment\dto\"
    "dto\EquipmentDto.java" = "features\equipment\dto\"
    
    # Profile Feature
    "controller\ProfileController.java" = "features\profile\controller\"
    "service\ProfileService.java" = "features\profile\service\"
    "dto\ProfileDto.java" = "features\profile\dto\"
    "dto\UpdateProfileDto.java" = "features\profile\dto\"
    
    # Request Feature
    "controller\RequestController.java" = "features\request\controller\"
    "service\RequestService.java" = "features\request\service\"
    "repository\EquipmentRequestRepository.java" = "features\request\repository\"
    "entity\EquipmentRequest.java" = "features\request\entity\"
    "dto\CreateRequestDto.java" = "features\request\dto\"
    "dto\EquipmentRequestDto.java" = "features\request\dto\"
    "dto\UpdateRequestStatusDto.java" = "features\request\dto\"
    
    # User Feature
    "repository\UserRepository.java" = "features\user\repository\"
    "entity\User.java" = "features\user\entity\"
}

foreach ($source in $moves.Keys) {
    $dest = $moves[$source]
    if (Test-Path $source) {
        Copy-Item $source $dest -Force
        Write-Host "  ✓ Copied $source"
    } else {
        Write-Host "  ✗ Source not found: $source" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Step 3: Summary ===" -ForegroundColor Green
Write-Host "Directory structure created successfully!"
Write-Host "All files have been copied to their feature locations."
Write-Host "Next steps:"
Write-Host "1. Update package declarations in all moved files"
Write-Host "2. Update @ComponentScan in UniGearTrackerApplication.java"
Write-Host "3. Run 'mvn clean compile' to verify"
