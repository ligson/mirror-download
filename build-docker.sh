#!/bin/bash
export JAVA_HOME=$JAVA_17_HOME
pkgName="mirror-download"
mvn clean
rm -rf target/*
mvn package -Dmaven.test.skip=true
cd target
pkVersion=`ls|grep jar|sed 's/.jar//'|sed 's/mirror-download-//'`
mv ${pkgName}-${pkVersion}-${pkgName}.tar.gz ${pkgName}.tar.gz
mkdir docker-tmp
tar -zxvf ${pkgName}.tar.gz -C docker-tmp/
cd docker-tmp
mv ${pkgName}-${pkVersion} ${pkgName}
cp ../../Dockerfile .
cp ../../entrypoint.sh .
docker build -t dockerhub.yonyougov.top/public/${pkgName}:$pkVersion .
docker push dockerhub.yonyougov.top/public/${pkgName}:$pkVersion


