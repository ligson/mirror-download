#!/bin/bash
export JAVA_HOME=$JAVA_17_HOME
pkgName="mirror-download"
mvn clean
rm -rf target/*
mvn package -Dmaven.test.skip=true
cd target
pkVersion=`ls|grep jar|sed 's/.jar//'|sed 's/mirror-download-//'`
mv mirror-download-${pkVersion}-mirror-download.tar.gz mirror-download.tar.gz
mkdir docker-tmp
tar -zxvf mirror-download.tar.gz -C docker-tmp/
cd docker-tmp
mv mirror-download-${pkVersion} mirror-download
cp ../../Dockerfile .
cp ../../entrypoint.sh .
docker build -t dockerhub.yonyougov.top/public/mirror-download:$pkVersion .
docker push dockerhub.yonyougov.top/public/mirror-download:$pkVersion


