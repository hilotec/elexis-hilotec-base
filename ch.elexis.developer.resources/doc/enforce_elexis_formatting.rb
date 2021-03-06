#!/usr/bin/env ruby
# Copyright 2011 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# Based on http://www.peterfriese.de/formatting-your-code-using-the-eclipse-code-formatter/
# 
# The config file was generated by setting the project specific ElexisFormatterProfile
# Then it is found in the project under .settings.
# Manually I had to add three lines to tell, that we use Java 1.5 constructs
#
CONFIG=File.expand_path("#{File.dirname(__FILE__)}/org.eclipse.jdt.core.prefs")

# CONFIG=File.expand_path("#{File.dirname(__FILE__)}/tst.xml")

# ECLIPSE=`which eclipse`.chomp
ECLIPSE='eclipse'
if ARGV.length != 1
  puts "Missing dir argument. "
  puts "   #{__FILE__} will enforce the ElexisFormatterProfile for all *.java files below"
  exit 2
end

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

dir = ARGV.shift
Dir.chdir(dir)
files = Dir.glob("**/*.java")
cmd="#{ECLIPSE} -application org.eclipse.jdt.core.JavaCodeFormatter -verbose " +
    "-config #{CONFIG} #{files.join(' ')}"
system(cmd)