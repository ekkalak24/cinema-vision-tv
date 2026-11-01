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
$layoutlibJar = "C:\Program Files\Android\Android Studio\plugins\android-layoutlib\lib\layoutlib.jar"
$layoutlibApiJar = "C:\Program Files\Android\Android Studio\plugins\android-layoutlib\lib\layoutlib-api.jar"
$coroutinesJar = "C:\Program Files\Android\Android Studio\lib\kotlinx-coroutines-core-1.3.4.jar"
$kotlinStdlib = "$kotlinHome\lib\kotlin-stdlib.jar"
$projectRoot = Split-Path -Parent $PSScriptRoot
$media3StubJar = Join-Path $projectRoot "libs\media3-stubs.jar"
$outDir = Join-Path $projectRoot "build\test-classes"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$mainFiles = Get-ChildItem -Path (Join-Path $projectRoot "app\src\main\java") -Recurse -Filter "*.kt" | Where-Object { $_.Name -notlike "MainActivity.kt" } | Select-Object -ExpandProperty FullName
$testFiles = Get-ChildItem -Path (Join-Path $projectRoot "app\src\test\java") -Recurse -Filter "*.kt" | Select-Object -ExpandProperty FullName

$cp = "$junitJar;$hamcrestJar;$kotlinStdlib;$layoutlibJar;$layoutlibApiJar;$coroutinesJar;$media3StubJar"
$compileArgs = @("-kotlin-home", $kotlinHome, "-cp", $cp, "-d", $outDir) + $mainFiles + $testFiles

Write-Host "> Task :app:compileTestKotlin"
& $java -cp $kotlincPreloader org.jetbrains.kotlin.preloading.Preloader -cp $kotlincCompiler org.jetbrains.kotlin.cli.jvm.K2JVMCompiler $compileArgs

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed"
    exit $LASTEXITCODE
}

Write-Host "> Task :app:test"
& $java -cp "$outDir;$junitJar;$hamcrestJar;$kotlinStdlib;$layoutlibJar;$layoutlibApiJar;$coroutinesJar;$media3StubJar" org.junit.runner.JUnitCore com.cinemavision.tv.domain.ColorRangeTest com.cinemavision.tv.domain.PatternStepTest com.cinemavision.tv.domain.CinemaAccuracyCalculatorTest com.cinemavision.tv.domain.BrandGuidanceTest com.cinemavision.tv.ui.pattern.PatternGeometryTest com.cinemavision.tv.ui.player.JudderPlayerManagerTest

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "BUILD SUCCESSFUL"
