#!/usr/bin/env ruby
# Niklaus Giger (c) 2010 niklaus.giger@member.fsf.org
#
# Supported targets:
#	linux
#	linux.x86_64
#	windows (32 bit)
#
# 2010.04.11	Simple setup for a Eclipse-RCP environment for elexis
# 2010.07.10    Update for new build.xml & Helios
# 2010.08.14    Added support linux.x86_64
#
require 'ftools'
require 'fileutils'

EclipseVers = "helios"
savedDir = File.expand_path(File.dirname(__FILE__))
searchDir = Dir.pwd
while true
  ld = "#{File.dirname(searchDir)}/downloads"
  if File.directory?(ld) then
    HudsonRoot = File.dirname(searchDir.clone)
    break
  else
    if searchDir.length <=1 or (searchDir == File.dirname(searchDir)) then
      puts "did not find a directory downloads in a directory form here to the top"
      exit 2
    end
  end
  searchDir = File.dirname(searchDir)
end
platformRuntime = File.expand_path(File.dirname(__FILE__)+"/../#{EclipseVers}")
puts "HudsonRoot is #{HudsonRoot}" if $VERBOSE
puts "platformRuntime ist #{platformRuntime}" if $VERBOSE

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

eclipse = "#{platformRuntime}/eclipse"
FileUtils.mkdir_p(platformRuntime) if !File.directory?(platformRuntime)
Dir.chdir(platformRuntime)
from=nil
platformRuntimeTar = nil
cmd = nil
if /x86_64-linux/i.match RUBY_PLATFORM
  platformRuntimeTar =
  "#{HudsonRoot}/downloads/eclipse-rcp-#{EclipseVers}-linux-gtk-x86_64.tar.gz"
  if !File.exists?(eclipse)
    cmd = "tar -zxf #{platformRuntimeTar}"
  end
elsif /linux/i.match RUBY_PLATFORM
  platformRuntimeTar =
  "#{HudsonRoot}/downloads/eclipse-rcp-#{EclipseVers}-linux-gtk.tar.gz"
  if !File.exists?(eclipse)
    cmd = "tar -zxf #{platformRuntimeTar}"
  end
elsif /mingw|msys|mswin/i.match RUBY_PLATFORM
  platformRuntimeTar = "#{HudsonRoot}/downloads/eclipse-rcp-#{EclipseVers}-win32.zip"
  if !File.exists?(eclipse+".exe")
    cmd = "unzip #{platformRuntimeTar}".gsub('/','\\\\')
  end
else
  puts "Unsupported "+RUBY_PLATFORM
  exit 2
end

if !File.exists?(platformRuntimeTar) then
	puts "File to extract #{platformRuntimeTar} missing"
	exit 3
end
if File.directory?(eclipse) then
	puts "Skipping #{cmd}"
else
	system(cmd)
end

origin=File.dirname(File.dirname(savedDir))
to   = "#{savedDir}/local.properties"
properties    = File.open(to, "w+")
properties.puts("skipPlugins=archie-patientstatistik,Statistics,StatisticsFragmentExample,"+
         "hilotec-pluginstatistiken,elexis-buchhaltung-basis,hilotec-messwerte,"+
	 "marlovits-addressSearch,marlovits-vornamen,marlovits-plz,elexis-notes")
properties.puts("base=#{origin}")
properties.puts("hg=hg")
properties.puts("repositories=#{origin}")
properties.puts("rsc=#{savedDir}/rsc")
properties.puts("platform-runtime=#{platformRuntime}/eclipse")
properties.puts("output=#{File.expand_path(File.dirname(savedDir))}/deploy")
# properties.puts("unplugged=true")
Dir.chdir("#{savedDir}/..")
if !File.directory?("archie")
	system("svn --quiet checkout http://archie.googlecode.com/svn/archie/ch.unibe.iam.scg.archie/branches/elexis-2.1");
end
