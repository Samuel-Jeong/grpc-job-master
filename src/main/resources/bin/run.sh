#!/bin/bash

PROFILES_ACTIVE=local

MAIN_CLASS_NAME=JobMasterAppDemoApplication
SERVICE_HOME=/home/ec2-user/dovaj/job-system/job-master-app
JAR_FILE_PATH=$SERVICE_HOME/libs
JAR_FILE_NAME=job-master-app-demo-0.0.1-SNAPSHOT.jar
PATH_TO_JAR=$JAR_FILE_PATH/$JAR_FILE_NAME

JAVA_OPT="$JAVA_OPT -XX:+UseG1GC \
-XX:NewRatio=2 \
-XX:SurvivorRatio=6 \
-XX:G1RSetUpdatingPauseTimePercent=5 \
-XX:MaxGCPauseMillis=500 \
-Xms512m -Xmx512m"
JAVA_OPT="$JAVA_OPT -Xlog:gc=debug:file=$SERVICE_HOME/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=100m"
JAVA_OPT="$JAVA_OPT -Dspring.profiles.active=$PROFILES_ACTIVE"


function exec_start() {
        PID=`ps -ef | grep java | grep ${MAIN_CLASS_NAME} | awk '{print $2}'`
        if ! [ -z "$PID" ]
        then
                echo "[${JAR_FILE_NAME}] is already running"
        else
                #ulimit -n 65535
                #ulimit -s 65535
                #ulimit -u 10240
                #ulimit -Hn 65535
                #ulimit -Hs 65535
                #ulimit -Hu 10240

                #java -jar $JAVA_OPT $PATH_TO_JAR ${MAIN_CLASS_NAME} > /dev/null 2>&1 &
                java $JAVA_OPT -Dspring.profiles.active=$PROFILES_ACTIVE -jar $PATH_TO_JAR ${MAIN_CLASS_NAME} > /dev/null 2>&1 &
                echo "[${JAR_FILE_NAME}] started ..."
        fi
}

function exec_stop() {
        PID=`ps -ef | grep java | grep ${MAIN_CLASS_NAME} | awk '{print $2}'`
        if [ -z "$PID" ]
        then
                echo "[${JAR_FILE_NAME}] is not running"
        else
                echo "stopping [${JAR_FILE_NAME}]"
                kill "$PID"
                sleep 1
                PID=`ps -ef | grep java | grep ${MAIN_CLASS_NAME} | awk '{print $2}'`
                if [ ! -z "$PID" ]
                then
                        echo "kill -9 ${PID}"
                        kill -9 "$PID"
                fi
                echo "[${JAR_FILE_NAME}] stopped"
        fi
}

function exec_status() {
  PID=`ps -ef | grep java | grep ${MAIN_CLASS_NAME} | awk '{print $2}'`
        if [ -z "$PID" ]
        then
                echo "[${JAR_FILE_NAME}] is not running"
        else
                echo "[${JAR_FILE_NAME}] is running"
          ps -aux | grep ${MAIN_CLASS_NAME} | grep "$PID"
        fi
}

case $1 in
    restart)
                exec_stop
                exec_start
                ;;
    start)
                exec_start
    ;;
    stop)
                exec_stop
    ;;
    status)
    exec_status
    ;;
esac