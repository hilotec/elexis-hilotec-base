#!/usr/bin/env ruby

# (c) 2010, Niklaus Giger <niklaus.giger@member.fsf.org>
#
#  This is a first attempt to use buildr.apache.org as a tool
#  to build elexis and its plugin in more automated fashion
# 
# TODO:	Make it work as good as ant
# TODO:	SWTbot 
# TODO: Add support for archie, elexis-addons, +++
# TODO: Port it to MacOSX, Windows
#
# Questions:
# What is the differences between *.jar in projects and *.jar in lib subdirectories?
#
top = File.dirname(File.dirname(__FILE__))

# Add default settings for buildr (~/.buildr.settings.yaml) if not present.
Settings= <<EOF
# Deploy server
server: localhost
usr: niklaus.giger@member.fsf.org
pwd: secret
repositories:
  remote:
   - http://www.ibiblio.org/maven2/
  release_to:
   - sftp://localhost/home/niklaus/maven-elexis-repo
EOF
settingsName = "#{ENV['HOME']}/.buildr/settings.yaml"
File.open(settingsName, 'w+') { |f| f.puts Settings } if !File.exists?(settingsName)

Header = <<EOF
# Please do not edit manually this file. Instead update
# #{File.expand_path(__FILE__)}
# to fix the problem!
  
# Version number for this release
VERSION_NUMBER = "1.0.0"
# Group identifier for your projects
GROUP = "elexisAll"
COPYRIGHT = ""

require 'buildr4osgi'
layout = Layout.new
layout[:source, :main, :java] = 'src'

desc "The elexisAllproject"
define "elexisAll", :layout => layout  do
  # Needed for annotiantions like @Override
  compile.options.source = "1.6"
  compile.options.target = "1.6"
  package_with_javadoc
#  package_with_sources

  eclipse.natures :plugin
  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
EOF

IgnoreProjects = [ 'hilotec-pluginstatistiken', 
                   'hilotec-messwerte', 
                   'elexis-connect-sysmex',
                   'elexis-developer-resources'
                 ] # 'elexis-utilities', 

buildfile = "#{top}/buildfile"
ausgabe = File.open(buildfile,"w+")

ausgabe.puts Header

Dir.glob('*/lib').each{ |libDir|
    puts "Adding #{libDir}";
   ausgabe.puts " OSGi.registry.containers << '#{libDir}'" 
} if false # Does not work

Dir.glob("*").each{ 
  |x| next if !File.directory?(x)
  next if IgnoreProjects.index(File.basename(x))
  ausgabe.puts "  define '#{x}', :layout => layout   do"
  jars = Dir.glob("#{x}/lib/*.jar")+Dir.glob("#{x}/*.jar")
  jars.each{ |jar| ausgabe.puts "    compile.dependencies <<  '#{jar}'"}
  ausgabe.puts "    compile.with project.dependencies"
  ausgabe.puts "    package(:plugin)"
  ausgabe.puts "  end"
}
ausgabe.puts "end"

puts "This run should have generated a #{buildfile}"
puts "  Usually you will continue with (2 first to step are only needed once"
puts "  or when dependencies/imports of projects have changed"
puts "  add --trace to calls to buildr to see more details"
puts "export  OSGi=/home/src/galileo2/eclipse"
puts "jruby -S buildr osgi:clean:dependencies  osgi:resolve:dependencies"
puts "jruby -S buildr compile" 
puts "jruby -S buildr install" 
