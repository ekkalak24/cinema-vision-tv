param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Args
)

$java = "C:\Program Files\Android\Android Studio\jre\bin\java.exe"
$kotlincPreloader = "C:\Program Files\Android\Android Studio\plugins\Kotlin\kotlinc\lib\kotlin-preloader.jar"
$kotlincCompiler = "C:\Program Files\Android\Android Studio\plugins\Kotlin\kotlinc\lib\kotlin-compiler.jar"
$kotlinHome = "C:\Program Files\Android\Android Studio\plugins\Kotlin\kotlinc"
$junitJar = "C:\Program Files\Android\Android Studio\lib\junit-4.12.jar"
$hamcrestJar = "C:\Program Files\Android\Android Studio\lib\hamcrest-core-1.3.jar"
$kotlinStdlib = "$kotlinHome\lib\kotlin-stdlib.jar"
$projectRoot = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $projectRoot "build\test-classes"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$mainFiles = Get-ChildItem -Path (Join-Path $projectRoot "app\src\main\java\com\cinemavision\tv\domain") -Filter "*.kt" | Select-Object -ExpandProperty FullName
$testFiles = Get-ChildItem -Path (Join-Path $projectRoot "app\src\test\java\com\cinemavision\tv\domain") -Filter "*.kt" | Select-Object -ExpandProperty FullName

$compileArgs = @("-kotlin-home", $kotlinHome, "-cp", "$junitJar;$hamcrestJar;$kotlinStdlib", "-d", $outDir) + $mainFiles + $testFiles

Write-Host "> Task :app:compileTestKotlin"
& $java -cp $kotlincPreloader org.jetbrains.kotlin.preloading.Preloader -cp $kotlincCompiler org.jetbrains.kotlin.cli.jvm.K2JVMCompiler $compileArgs

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed"
    exit $LASTEXITCODE
}

Write-Host "> Task :app:test"
& $java -cp "$outDir;$junitJar;$hamcrestJar;$kotlinStdlib" org.junit.runner.JUnitCore com.cinemavision.tv.domain.ColorRangeTest com.cinemavision.tv.domain.PatternStepTest com.cinemavision.tv.domain.CinemaAccuracyCalculatorTest com.cinemavision.tv.domain.BrandGuidanceTest

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "BUILD SUCCESSFUL"
