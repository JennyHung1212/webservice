#!/bin/bash

sudo yum update -y
sudo yum install -y java-1.8.0-openjdk.x86_64
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2022
sudo wget https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm
yes | sudo yum localinstall mysql57-community-release-el7-11.noarch.rpm
yes | sudo yum install mysql-community-server
sudo systemctl start mysqld
default_password=$(sudo grep 'temporary password' /var/log/mysqld.log)
echo "$default_password"
delimiter="root@localhost: "
string=$default_password$delimiter
strarr=()
while [[ $string ]]; do
  strarr+=( "${string%%"$delimiter"*}" )
  string=${string#*"$delimiter"}
done
default_password=${strarr[1]}
echo "$default_password"
sudo mysqladmin -u root -p"$default_password" password "Password**123"