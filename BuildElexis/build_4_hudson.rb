#!/usr/bin/env ruby
# Niklaus Giger (c) 2010 niklaus.giger@member.fsf.org
#
# 2010.04.11	Simple setup for a Eclipse-RCP environment for elexis
#
require 'ftools'
require 'fileutils'

savedDir = File.expand_path(File.dirname(__FILE__))
# We might run in different setups.
# But below we do not want any differnces
searchDir = Dir.pwd
while true
	ld = "#{File.dirname(searchDir)}/downloads"
	if File.directory?(ld) then
		HudsonRoot = File.dirname(searchDir.clone)
		puts "HudsonRoot is #{HudsonRoot}"
		break
	else
		if searchDir.length <=1 or (searchDir == File.dirname(searchDir)) then
		puts "downloads nirgends gefunden"
		exit 2
		end
	end
	searchDir = File.dirname(searchDir)
end
HudsonRcp  = "#{HudsonRoot}/rcpbase"
HudsonArgieZip = "#{HudsonRoot}/downloads/archie-1.0.2.zip"
HudsonJintoZip = "#{HudsonRoot}/downloads/de.guhsoft.jinto-0.13.5.zip"
HudsonFindbugs = "#{HudsonRoot}/downloads/findbugs-1.3.9.zip"
# http://downloads.sourceforge.net/project/findbugs/findbugs/1.3.9/.tar.gz?use_mirror=surfnet
if /mswin/i.match RUBY_PLATFORM
	RcpBase = "#{HudsonRcp}/windows"
elsif /linux/i.match RUBY_PLATFORM
	RcpBase = "#{HudsonRcp}/linux"
elsif /macos/i.match RUBY_PLATFORM
	RcpBase = "#{HudsonRcp}/mac"
else
	puts "Unknown Ruby-Platform #{RUBY_PLATFORM}"
	exit 2
end
# We assume that we find the correct files under
HudsonDownloads = [
  HudsonArgieZip,
  HudsonJintoZip,
  HudsonFindbugs,
  ]
# Automatisches Aufsetzen eine Workspaces für Hudson
# wget -c http://www.sliksvn.com/pub/Slik-Subversion-1.6.9-win32.msi
# wget -c http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/galileo/SR2/eclipse-rcp-galileo-SR2-win32.zip&url=http://mirror.switch.ch/eclipse/technology/epp/downloads/release/galileo/SR2/eclipse-rcp-galileo-SR2-win32.zip&mirror_id=63
HudsonDownloads.each{ |x|
  if !File.exists?(x)
    puts "Missing file #{x}"
    exit 2
  end
}

DryRun = false
def system(cmd, mayFail=false)
  puts "cd #{Dir.pwd} && #{cmd} # mayFail #{mayFail}"
  if DryRun then return
  else res =Kernel.system(cmd)
  end
  if !res and !mayFail then
    puts "running #{cmd} #{mayFail} failed"
    exit
  end
end

FileUtils.mkdir_p(RcpBase) if !File.directory?(RcpBase)
eclipse = "#{RcpBase}/rcp"
Dir.chdir(RcpBase)
if /linux/i.match RUBY_PLATFORM
	from = "#{savedDir}/rsc/build/local.properties.hudson_linux"
	to   = "#{savedDir}/rsc/build/local.properties"
	File.copy(from, to, :verbose => true)
	HudsonRcpTar = "#{HudsonRoot}/downloads/eclipse-rcp-galileo-SR2-linux-gtk.tar.gz"
	if !File.exists?(eclipse)
		system("tar -zxf #{HudsonRcpTar}")
		system("mv eclipse rcp")
	end
elsif /mswin/i.match RUBY_PLATFORM
	from = "#{savedDir}/rsc/build/local.properties.hudson_win"
	to   = "#{savedDir}/rsc/build/local.properties"
	File.copy(from, to, :verbose => true)
	HudsonRcpTar = "#{HudsonRoot}/downloads/eclipse-rcp-galileo-SR2-win32.zip"
	if !File.exists?(eclipse+".exe")
		system("unzip #{HudsonRcpTar}".gsub('/','\\\\'))
	end if false
else
	puts "Unsupported "+RUBY_PLATFORM
	exit 2
end

Dir.chdir(RcpBase)
datei="#{RcpBase}/findbugs-1.3.9/lib/findbugs-ant.jar"
if Dir.glob(datei).size == 0 # works on Debian Lenny, but elsewhere?
  system("unzip #{HudsonFindbugs}")
  system("cp #{datei} /usr/share/ant/lib")
end

Dir.chdir(eclipse)
datei="#{eclipse}/plugins/ch.unibe.iam*.jar"
if Dir.glob(datei).size == 0
  system("unzip #{HudsonArgieZip}")
end
if Dir.glob("#{eclipse}/features/*jinto*").size == 0
  system("unzip #{HudsonJintoZip}")
end

Dir.chdir(savedDir+"/rsc/build")
cmd = "ant hudson"
system(cmd)
