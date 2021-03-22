#!/bin/bash
app_name='eliona-koppler'

find src/ -name "*.java" > sources.txt
find thirdparty/json-simple/src/main/ -name "*.java" >> sources.txt

javac @sources.txt -d build

if [[ $? = 0 ]];
then
  # copy keystore to build env
  mkdir build/ch/itec/keystore
  cp keystore/niagara.ks build/ch/itec/keystore/

  # cp -r META-INF build/
  cd build
  jar cmvf ../META-INF/MANIFEST.MF $app_name.jar .
else
  echo "ERROR"
fi;
