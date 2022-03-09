sudo wget http://nginx.org/keys/nginx_signing.key
sudo amazon-linux-extras enable epel
yes | sudo yum install epel-release
sudo rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY*
yes | sudo yum install nginx
yes | sudo systemctl start nginx
yes | sudo systemctl enable nginx
cd /etc/systemd/system
sudo touch app.service
sudo chmod 766 app.service
sudo echo -e "[Unit]\\nDescription=Spring Boot HelloWorld\\nAfter=syslog.target\\nAfter=network.target[Service]\\nUser=ec2-user\\nType=simple\\n\\n[Service]\\nExecStart=/usr/bin/java -jar /home/ec2-user/webservice-0.0.1-SNAPSHOT.jar\\nRestart=always\\nStandardOutput=syslog\\nStandardError=syslog\\nSyslogIdentifier=helloworld\\n\\n[Install]\\nWantedBy=multi-user.target" > app.service
sudo systemctl start app
sudo systemctl enable app
echo $(systemctl status app)