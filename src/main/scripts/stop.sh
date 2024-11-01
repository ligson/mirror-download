#!/bin/bash
echo "开始停止mirror-download..."
PID=`cat mirror-download.pid`
kill -9 $PID
echo "已经停止mirror-download,PID:$PID"
