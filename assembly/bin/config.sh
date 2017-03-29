#!/usr/bin/env bash
# The directory in which your application is installed
APP_DIR="/web/patch-builder-with-maven"
APP_WEB="${APP_DIR}/webapp"
APP_VERSION="1.0.0"
APP_NAME="patch-builder-with-maven"
APP_EN_NAME="patch-builder-with-maven"
# *NOTE* 多网卡下不正确
IP="$( LC_ALL=C ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' |cut -d: -f2 | awk '{ print $1}' )"
COUNT=1
# 不同机器应配置不同的标识，建议用ip:counter形式表示。
APP_ID="${IP}:${COUNT}"
# The fat jar containing your application
APP_JAR="${APP_DIR}/${APP_EN_NAME}-${APP_VERSION}.jar"

APP_CLASSPATH="${APP_DIR}/conf:${APP_DIR}/lib/*.jar"
APP_CONF_DIR="${APP_DIR}/conf"
APP_CONF_FILE="${APP_CONF_DIR}/socialityservice.json"
APP_CONF_LOG4J_FILE="file:${APP_CONF_DIR}/log4j.properties"
# The application argument such as -cluster -cluster-host ...
APP_ARGS="-cluster -instances 2 -conf ${APP_CONF_FILE}"
#APP_MAIN_CLASS=

# JVM options and system properties (-Dfoo=bar).
JAVA_OPTS="${JAVA_OPTS} -Xmn128M -Xss256K \
 -XX:+DisableExplicitGC -XX:SurvivorRatio=1 \
 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC \
 -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled \
 -XX:+UseFastAccessorMethods \
 -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 \
 -XX:SoftRefLRUPolicyMSPerMB=0 \
 -Djava.awt.headless=true \
 -Dapp.name=${APP_EN_NAME} \
 -Dapp.id=${APP_ID}"

# vert.x options and system properties (-Dfoo=bar).
VERTX_OPTS="${JAVA_OPTS} -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4jLogDelegateFactory \
 -Dlog4j.configurationFile=${APP_CONF_LOG4J_FILE} \
 -Dvertx.pool.worker.size=40\
 -Dvertx.cwd=${APP_WEB}"

# ***********************************************
OUT_FILE=/log/${APP_EN_NAME}-out.log
RUNNING_PID="${APP_DIR}"/RUNNING_PID
# ***********************************************