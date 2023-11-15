FROM mcr.microsoft.com/windows/server:ltsc2022

# Enable Remote Desktop
RUN powershell -Command \
    Add-WindowsFeature RDS-RD-Server; \
    Install-WindowsFeature -Name RDS-Web-Access -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature RDS-Web-Access -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature -Name RDS-Gateway -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature RDS-Gateway -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature RDS-Licensing -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature -Name RDS-Licensing -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature -Name RDS-Connection-Broker -IncludeAllSubFeature -IncludeManagementTools; \
    Install-WindowsFeature RDS-Connection-Broker -IncludeAllSubFeature -IncludeManagementTools; 

