#!/bin/bash

mkdir keystore

keytool -genkeypair -keyalg RSA -alias self_signed -keypass N1agArA.Eli0na \
  -keystore keystore/niagara.ks -storepass N1agArA.Eli0na

