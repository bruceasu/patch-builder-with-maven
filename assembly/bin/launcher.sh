#!/bin/bash

###
# chkconfig: 345 20 80
# description: Vert.x application service script
# processname: java
#
# Installation (CentOS):
# copy file to /etc/init.d
# chmod +x /etc/init.d/launcher.sh
# chkconfig --add /etc/init.d/launcher.sh
# chkconfig launcher.sh on
#
# Installation (Ubuntu):
# copy file to /etc/init.d
# chmod +x /etc/init.d/launcher.sh
# update-rc.d launcher.sh defaults
#
#
# Usage: (as root)
# service launcher.sh start
# service launcher.sh stop
# service launcher.sh status
#
###

# 获取脚本所在的目录的绝对路径
function get_abs_dir() {
    SOURCE="${BASH_SOURCE[0]}"
    # resolve $SOURCE until the file is no longer a symlink
    while [ -h "$SOURCE" ]; do
        TARGET="$(readlink "$SOURCE")"
        if [[ ${SOURCE} == /* ]]; then
            # echo "SOURCE '$SOURCE' is an absolute symlink to '$TARGET'"
            SOURCE="$TARGET"
        else
            DIR="$(dirname "$SOURCE")"
            # echo "SOURCE '$SOURCE' is a relative symlink to '$TARGET' (relative to '$DIR')"
            # if $SOURCE was a relative symlink, we need to resolve it
            # relative to the path where the symlink file was located
            SOURCE="$DIR/$TARGET"
        fi
    done
    # echo "SOURCE is '$SOURCE'"

    # RDIR="$( dirname "$SOURCE" )"
    DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
    # if [ "$DIR" != "$RDIR" ]; then
    #     echo "DIR '$RDIR' resolves to '$DIR'"
    # fi
    # echo "DIR is '$DIR'"
    echo $DIR
}

SCRIPT_DIR=`get_abs_dir`

mkdir -p /log

source "${SCRIPT_DIR}/config.sh"

# colors
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }



# Check whether the application is running.
# The check is pretty simple: open a running pid file and check that the process
# is alive.
isrunning() {
    # Check for running app
    if [ -f "${RUNNING_PID}" ]; then
        proc=$(cat ${RUNNING_PID});
        if /bin/ps --pid ${proc} 1>&2 >/dev/null;
        then
            return 0
        fi
    fi
    return 1
}

#启动方法
start(){
    if isrunning; then
        echoYellow "---------------The sociality-api is already running------------"
        return 0
    fi

    pushd ${APP_DIR} > /dev/null
    #exec java -Xms128m -Xmx2048m -jar $APP_JAR 5 >$OUT_FILE &
    nohup java ${VERTX_OPTS} -jar ${APP_JAR} ${APP_ARGS} > ${OUT_FILE} 2>&1 &
    echo $! > ${RUNNING_PID}
    popd > /dev/null

    if isrunning; then
        echoGreen "--------------The sociality-api started----------------------"
        exit 0
    else
        echoRed "The sociality-api has not started - check log"
        exit 3
    fi
    #方式一：java -jar XXX.jar
    #特点：当前ssh窗口被锁定，可按CTRL + C打断程序运行，或直接关闭窗口，程序退出
    #方式二: java -jar XXX.jar & ,&代表在后台运行,特定：当前ssh窗口不被锁定，但是当窗口关闭时，程序中止运行。
    #方式三:nohup java -jar XXX.jar &,nohup 意思是不挂断运行命令,当账户退出或终端关闭时,程序仍然运行,当用 nohup 命令执行作业时，缺省情况下该作业的所有输出被重定向到nohup.out的文件中，除非另外指定了输出文件
    #方式四:nohup java -jar XXX.jar >temp.txt &,command >out.file是将command的输出重定向到out.file文件，即输出内容不打印到屏幕上，而是输出到out.file文件中
    #java -Xms128m -Xmx2048m -jar test2.jar 5 > log.log &
    #tail -f result.log
}

#重启方法
restart() {
    echo "Restarting ${APP_NAME} with ${APP_JAR}"
    stop
    start
}

#停止方法
stop() {
    echoYellow "Stopping ${APP_NAME} with ${APP_JAR}"

    if isrunning; then
        kill `cat ${RUNNING_PID}`
        i=5;

        while [[ running && i -gt 0 ]]
        do
            echo -n "$i "
            sleep 1;
            ((i--))
        done;
        if isrunning; then
            kill -KILL `cat ${RUNNING_PID}`
        fi
        rm $RUNNING_PID
        echo -e "\n"
    fi
}

#查询运行状态方法
status() {
    if isrunning; then
        echoGreen "${APP_NAME} is running"
    else
        echoRed "${APP_NAME} is either stopped or inaccessible"
    fi
}

case "$1" in
    start)
        start
        ;;
    stop)
        if isrunning; then
            stop
            exit 0
        else
            echoRed "${APP_NAME} not running"
            exit 3
        fi
        ;;
    restart)
        stop
        start
        ;;
    status)
        status
        exit 0
        ;;
    *)
        printf 'Usage: %s {status|start|stop|restart}\n'
        exit 1
        ;;
esac
