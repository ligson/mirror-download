#!/bin/bash
echo "开始启动mirror-download..."
WORK_HOME=$(cd ../;pwd)

LIB_PATH=$WORK_HOME/conf
RUN_JAVA=$JAVA_HOME/bin/java

for jar in `ls $WORK_HOME/lib/*.jar`
	do LIB_PATH=$LIB_PATH:$jar
done

echo "$RUN_JAVA -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8 -classpath $LIB_PATH org.ligson.mirrordownload.AppBoot"
#export LD_LIBRARY_PATH='$LD_LIBRARY_PATH:$WORK_HOME/tools/lib'
nohup $RUN_JAVA -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8 -classpath $LIB_PATH org.ligson.mirrordownload.AppBoot >>mirror-download.log 2>1&
echo $!>mirror-download.pid
PID=`cat mirror-download.pid`
echo "启动mirror-download成功,PID:$PID"
