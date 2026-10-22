<#
  Inicia el entorno local de la Evaluación Parcial 2.
  Ejecútalo desde una consola PowerShell normal de Windows:
    .\iniciar-demo.ps1
#>

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$logsPath = Join-Path $projectRoot '.runtime-logs'
$cloudNativeRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $projectRoot))

$mavenCandidates = @(
  (Get-Command mvn -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue),
  (Join-Path $cloudNativeRoot 'Actividad-2.2.3-DLQ\.tools\apache-maven-3.10.0\bin\mvn.cmd')
) | Where-Object { $_ -and (Test-Path $_) }

if (-not $mavenCandidates) {
  throw 'No se encontró Maven. Instálalo o verifica la carpeta Actividad-2.2.3-DLQ/.tools.'
}

$maven = @($mavenCandidates)[0]
$backendMaven = Join-Path $projectRoot 'backend\mvnw.cmd'
New-Item -ItemType Directory -Force -Path $logsPath | Out-Null

Write-Host 'Levantando RabbitMQ...' -ForegroundColor Cyan
Push-Location $projectRoot
try {
  docker compose -f docker-compose.rabbitmq.yml up -d
} finally {
  Pop-Location
}

$services = @(
  @{ Name = 'backend'; Port = 8080; Path = 'backend'; File = $backendMaven; Arguments = @('-Dspring-boot.run.profiles=messaging-demo', 'spring-boot:run') },
  @{ Name = 'notificaciones'; Port = 8081; Path = 'microservices\notificaciones-service'; File = $maven; Arguments = @('spring-boot:run') },
  @{ Name = 'documentos'; Port = 8082; Path = 'microservices\documentos-service'; File = $maven; Arguments = @('spring-boot:run') },
  @{ Name = 'auditoria'; Port = 8083; Path = 'microservices\auditoria-service'; File = $maven; Arguments = @('spring-boot:run') },
  @{ Name = 'rabbit-admin'; Port = 8084; Path = 'microservices\rabbitmq-admin-service'; File = $maven; Arguments = @('spring-boot:run') }
)

foreach ($service in $services) {
  if (Get-NetTCPConnection -LocalPort $service.Port -State Listen -ErrorAction SilentlyContinue) {
    Write-Host "$($service.Name) ya está disponible en el puerto $($service.Port)." -ForegroundColor Yellow
    continue
  }
  $servicePath = Join-Path $projectRoot $service.Path
  $outLog = Join-Path $logsPath "$($service.Name).out.log"
  $errLog = Join-Path $logsPath "$($service.Name).err.log"
  Write-Host "Iniciando $($service.Name)..." -ForegroundColor Cyan
  Start-Process -FilePath $service.File -ArgumentList $service.Arguments -WorkingDirectory $servicePath -WindowStyle Minimized -RedirectStandardOutput $outLog -RedirectStandardError $errLog
}

Write-Host ''
Write-Host 'Servicios iniciados. Espera aproximadamente 30 segundos y revisa:' -ForegroundColor Green
Write-Host '  RabbitMQ nodo 1: http://localhost:15672  (guest/guest)'
Write-Host '  RabbitMQ nodo 2: http://localhost:15673  (guest/guest)'
Write-Host '  Backend:          http://localhost:8080'
Write-Host '  Administración:   http://localhost:8084/api/rabbitmq/queues'
Write-Host "  Logs:             $logsPath"
