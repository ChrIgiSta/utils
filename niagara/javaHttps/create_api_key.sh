#!/bin/bash

echo "Plain Key"
key=$(hexdump -n 16 -e '4/4 "%08X" 1 "\n"' /dev/random)
echo $key

echo "Key Base64 encoded (as you need to send)"
keyB64=$(echo $key | base64)
echo $keyB64

echo "MD5 Key to install in server"
keymd5=$(echo $key | md5sum)
echo $keymd5
