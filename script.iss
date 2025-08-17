; Script per Inno Setup

[Setup]
AppName=Personal Finance Tracker
AppVersion=1.0
DefaultDirName={autopf}\Personal Finance Tracker
DefaultGroupName=Personal Finance Tracker
OutputDir=.\Output
OutputBaseFilename=Setup_FinanceTracker
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "Personal Finance Tracker\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTA: "Personal Finance Tracker" è la cartella creata da jpackage

[Icons]
Name: "{group}\Personal Finance Tracker"; Filename: "{app}\Personal Finance Tracker.exe"
Name: "{autodesktop}\Personal Finance Tracker"; Filename: "{app}\Personal Finance Tracker.exe"; Tasks: desktopicon