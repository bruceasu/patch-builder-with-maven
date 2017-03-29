#!/usr/bin/env bash
# encoding: utf-8
#
# 使用方法：
#　　此文件放在补丁文件同一个目录下
#    提供一个更新说明文件，update-${APP_EN_NAME}-${APP_VERSION}.txt
#        例如： update-sociality-service-1.0.1.txt
#　　补丁目录结构（例如）：
#　　update-files-dir:
#        updater.sh
#        update-${APP_EN_NAME}-${APP_VERSION}.txt
#        ${APP_EN_NAME}-${APP_VERSION}.jar
#        bin/launcher.sh
#        bin/config.sh
#        conf/cluster.xml
#        conf/log4j.properties
#        conf/logging.properties
#        conf/config.json
#        lib/...
#        update/...

# 初始化
# 文字颜色
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

# 分隔行
SHELL_LINE="======================================================================"
# 应用名称
APP_NAME="patch-builder-with-maven"
# 应用名称（英文），用于组装路径、文件名等
APP_EN_NAME="patch-builder-with-maven"
# 本次发布版本
APP_VERSION="1.0.0"
# 本次发布说明, 描述更新的内容和文件列表，还可以添加svn等其他信息。
APP_UPDATE_MESSAGE_FILE="update-${APP_EN_NAME}-${APP_VERSION}.txt"
# changelog 文件，描述所有（历史）更新的信息
APP_CHANGELOG_FILE="change_log.txt"
# 部署目录
APP_TARGET_DIR="/web/sociality-service"

# define colorful echo functions
echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }

############################################################

# 获取脚本所在的目录的绝对路径
function get_abs_dir() {
    SOURCE="${BASH_SOURCE[0]}"
    # resolve ${SOURCE} until the file is no longer a symlink
    while [ -h "${SOURCE}" ]; do
        TARGET="$(readlink "${SOURCE}")"
        if [[ ${SOURCE} == /* ]]; then
            # echo "SOURCE '${SOURCE}' is an absolute symlink to '$TARGET'"
            SOURCE="$TARGET"
        else
            DIR="$(dirname "${SOURCE}")"
            # echo "SOURCE '${SOURCE}' is a relative symlink to '$TARGET' (relative to '$DIR')"
            # if ${SOURCE} was a relative symlink, we need to resolve it
            # relative to the path where the symlink file was located
            SOURCE="$DIR/$TARGET"
        fi
    done
    # echo "SOURCE is '${SOURCE}'"

    # RDIR="$( dirname "${SOURCE}" )"
    DIR="$( cd -P "$( dirname "${SOURCE}" )" && pwd )"
    # if [ "$DIR" != "$RDIR" ]; then
    #     echo "DIR '$RDIR' resolves to '$DIR'"
    # fi
    # echo "DIR is '$DIR'"
    echo ${DIR}
}

SCRIPT_DIR=`get_abs_dir`

function die {
    echoRed $1
    exit 1
}

# 先备份文件。
#
function backup_old_version() {
    cur_datetime=`date +%Y%m%d-%H%M%S`
    backup_dir="/backup/${APP_EN_NAME}"
    backup_file="$backup_dir/${APP_EN_NAME}-$cur_datetime.tar"
    mkdir -p ${backup_dir}
    echo "Backup the old version files: ${APP_TARGET_DIR} ..."
    (cd ${APP_TARGET_DIR} && \
            tar cvpf ${backup_file} .  --exclude=logs --exclude=.vertx  && \
            echoGreen "Backup files of old version to ${backup_file}" || \
                echoRed "The old version is not installed, skip backup action.")
    echoGreen "Backup finished."
    # need to clear file
    rm ${APP_TARGET_DIR}/${APP_EN_NAME}-*.jar
}

# 安装补丁、程序、配置文件
#
function install_files() {
    echo "Intalling files ... "
    # use install or cp to copy files to target directory
    # use install command is better than cp
    # but cp can copy the file tree, then install just file or file list.
    # windows 的msys 的install 不支持 -C 参数， 用-p 替换
    # 建目录
    (install -d ${APP_TARGET_DIR}/bin ${APP_TARGET_DIR}/conf \
             ${APP_TARGET_DIR}/lib \
             ${APP_TARGET_DIR}/update)
    # 复制脚本
    (cd ${SCRIPT_DIR}/bin && \
            install -b -C -m 777 launcher.sh config.sh ${APP_TARGET_DIR}/bin)

    # 复制配置文件
    (cd ${SCRIPT_DIR}/conf && \
            install -b -C -m 644 cluster.xml log4j.properties \
                    logging.properties socialityservice.json \
                    ${APP_TARGET_DIR}/conf)

    # 复制依赖库，不是每次更新都需要更新依赖包，按需处理
    (cd ${SCRIPT_DIR}/lib && install -C -m 644 *.jar *.zip ${APP_TARGET_DIR}/lib)

    # 复制主jar文件
    (cd ${SCRIPT_DIR} && \
            install -m 644 ${APP_EN_NAME}-${APP_VERSION}.jar ${APP_TARGET_DIR})

    (cd ${SCRIPT_DIR} && \
            install -p -m 644 ${APP_UPDATE_MESSAGE_FILE} ${APP_TARGET_DIR}/update)

    (cd ${SCRIPT_DIR} && \
            install -p -m 644 ${APP_CHANGELOG_FILE} ${APP_TARGET_DIR}/update)

    echoGreen "copy files finished."
}

# 修改配置文件
#
function configure_setting() {
    echoRed "Use sed or some auto editor to modify file(s),"
    echoRed "if can't simply replace by a file. "
    echoRed "Maybe show how to do for operator."
    #    echo  ${APP_TARGET_DIR}/bin/launcher.sh
    sed -i 's/^APP_VERSION=.\+$/APP_VERSION="'"${APP_VERSION}"'"/g' ${APP_TARGET_DIR}/bin/config.sh

    echoGreen "Configre setting finished."
}

# 启动程序
#
function startup() {
    ${APP_TARGET_DIR}/bin/launcher.sh start
}


# 停止程序
#
function shutdown() {
    ${APP_TARGET_DIR}/bin/launcher.sh stop
}

# 重启程序
#
function restart() {
    shutdown
    startup
}


function run {
    # 先停止程序
    # shutdown

    backup_old_version
    echoYellow ${SHELL_LINE}

    install_files
    echoYellow ${SHELL_LINE}

    configure_setting
    echoYellow ${SHELL_LINE}

    while [ 1 ]
    do
        #echoYellow "Start up the program right now [Y/N]?"
        #read next_action
        #if [[ $next_action == 'Y' || $next_action == 'y'  ]]; then
        #    restart # or startup if shutdown called!
        #    break
        #fi
        #if [[ $next_action == 'N' || $next_action == 'n'  ]]; then
        #    break
        #fi
        #echoRed "command error, please retry again."
        read -n1 -p "Do you want to start up the program right now [Y/N]?" next_action
        case ${next_action} in
            Y | y)
                restart # or startup if shutdown called!
                break;;
            N | n)
                echo ""
                break;;
            *)
                echo ""
                echoRed "command error, please retry again.";;
        esac

    done

    echoGreen "Deploy finished."
}


clear

echoYellow ${SHELL_LINE}
echoGreen "Start to install $APP_NAME ${APP_VERSION}"
echo "Message in Chinese(UTF-8) encoding."
cat "${APP_UPDATE_MESSAGE_FILE}"
echo ""
echo -e "\n\ninstall files from extracted directory: ${SCRIPT_DIR}\n"
echoGreen "Press any key to CONTINUE, or press Ctrl + C to ABORT"
read -n 1

# 执行
run