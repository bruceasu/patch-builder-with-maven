#!/usr/bin/python
# -*- coding: utf-8  -*-
# Install to Project Repo
# A script for installing jars to an in-project Maven repository. 
# v0.1.1
# 
# MIT License
# (c) 2012, Nikita Volkov. All rights reserved.
# http://github.com/nikita-volkov/install-to-project-repo
# 


import os
import sys
import re
import shutil


def jars(dir):
    return [dir + "/" + f for f in os.listdir(dir) if f.lower().endswith(".jar")]


def maven_dependencies(parsing_results):
    """ generate maven dependecy config.

    :param parsing_results:
    :return:
    """

    def maven_dependency(artifact):
        # print artifact
        if artifact.has_key("classifier") and artifact['classifier'] != '':
            return """
<dependency>
  <groupId>%(groupId)s</groupId>
  <artifactId>%(artifactId)s</artifactId>
  <version>%(version)s</version>
  <classifier>%(classifier)s</classifier>
</dependency>
""" % artifact
        else:
            return """
<dependency>
  <groupId>%(groupId)s</groupId>
  <artifactId>%(artifactId)s</artifactId>
  <version>%(version)s</version>
</dependency>
""" % artifact

    return "\n".join([maven_dependency(a).strip() for a in parsing_results])



def install(install_path, parsing):
    os.system(
        "mvn install:install-file" + \
        " -Dfile=" + parsing['jar'] + \
        " -DgroupId=" + parsing["groupId"] + \
        " -DartifactId=" + parsing["artifactId"] + \
        " -Dversion=" + parsing["version"] + \
        " -Dpackaging=jar" + \
        " -DlocalRepositoryPath=" + install_path + \
        " -DcreateChecksum=true" + \
        (" -Dclassifier=" +  parsing["classifier"] if parsing.has_key("classifier") and parsing["classifier"] != '' else "")
    )


def parse_interactively():
    ''' 录入相关参数。 '''
    path = raw_input('输入JAR文件：')
    groupId = raw_input('输入groupId：')
    artifactId = raw_input('输入artifactId：')
    classifier = raw_input('输入classifier：')
    version = raw_input('输入version：')
    return ({
        "jar": path,
        "groupId": groupId,
        "artifactId": artifactId,
        "version": version,
        "classifier": classifier
    })


user_home = os.path.expanduser('~')
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-i", "--interactive",
                  dest="interactive", action="store_true", default=False,
                  help="Interactively resolve ambiguous names. Use this option to install libraries of different naming standards")
parser.add_option("-d", "--delete",
                  dest="delete", action="store_true", default=False,
                  help="Delete successfully installed libs in source location")
parser.add_option("-a", "--jar",
                  dest="jarFile", action="store", default='xx.jar', metavar="FILE",
                  help="Set the jar file, default is %default")
parser.add_option("-p", "--path",
                  dest="install_path", action="store", default='%s/.m2/repository' % user_home,
                  metavar="INSTALL_PATH", help="Set the isntall path file, default is %default")
parser.add_option("-c", "--config",
                  dest="info", action="store",
                  metavar="config", help="Set the config. Format: groupId:artifactId:version:classifier")
parser.add_option("-f", "--config-file",
                  dest="info_file", action="store",
                  metavar="config-file",
                  help="Use a config file. Format for each line: 'file groupId:artifactId:version:classifier'")
(options, args) = parser.parse_args()

if options.info_file:
    # read file and parse
    try:
        f = open(options.info_file)
        cfgs = []
        for line in f:
            line = line.strip()
            if line == '' or line[0] == '#':
                continue
            jarFile, info = line.split("\s+");
            x = info.split(":")
            if (len(x) > 3) :
                groupId, artifactId, version, classifier = x
            elif (len(x) == 3):
                groupId, artifactId, version= x
            else:
                print "配置不正确", line
                continue
            cfgs.append({"jar": jarFile, "group": groupId, "artifactId": artifactId, "version": version,
                         "classifier": classifier})

        parsings = cfgs
    except Exception, e:
        print '读取配置文件错误', e
elif options.info:
    jarFile = options.jarFile
    x = options.info.split(":")
    if (len(x) > 3):
        groupId, artifactId, version, classifier = x
        parsings = [{
            "groupId": groupId,
            "artifactId": artifactId,
            "version": version,
            "classifier": classifier,
            "jar": jarFile
        }]
    elif (len(x) == 3):
        groupId, artifactId, version = x
        parsings = [{
            "groupId": groupId,
            "artifactId": artifactId,
            "version": version,
            "jar": jarFile
        }]
    else:
        print "配置不正确", options.info
        parser.print_help()
        sys.exit(1)
elif options.interactive:
    parsings = [parse_interactively()]
else:
    parser.print_help()
    sys.exit(1)

install_path = options.install_path
# print parsings

for parsing in parsings:
    install(install_path, parsing)
    if options.delete:
        os.remove(parsing["jar"])
    #print 'test: ', parsing
print maven_dependencies(parsings)