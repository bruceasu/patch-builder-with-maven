#!/usr/bin/env sh
echo off

# 获取APP所在的目录的绝对路径
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
    DIR="$( cd -P "$( dirname "$SOURCE" )" && cd .. && pwd )"
    # if [ "$DIR" != "$RDIR" ]; then
    #     echo "DIR '$RDIR' resolves to '$DIR'"
    # fi
    # echo "DIR is '$DIR'"
    echo $DIR
}

APP_HOME=`get_abs_dir`

LIB_DIR=${APP_HOME}/lib
PRG=${APP_HOME}/tools/install-to-project-repo.py

python ${PRG} -a ${LIB_DIR}/antlr-2.7.4.jar  -c  org.antlr:antlr:2.7.4:system
python ${PRG} -a ${LIB_DIR}/chardet-1.0.jar  -c  chardet:chardet:1.0:system
python ${PRG} -a ${LIB_DIR}/cpdetector_1.0.10.jar  -c  cpdetector:cpdetector:1.0.10:system
