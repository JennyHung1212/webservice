sudo chmod +x webservice-0.0.1-SNAPSHOT.jar
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
sudo echo -e "
[Unit]
Description=Spring Boot App
After=syslog.target
After=network.target[Service]
User=ec2-user
Type=simple

[Service]
ExecStart=/usr/bin/java -jar /home/ec2-user/webservice-0.0.1-SNAPSHOT.jar
Restart=always
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=webservice
EnvironmentFile=/etc/systemd/system/app.conf

[Install]
WantedBy=multi-user.target" > app.service
sudo systemctl start app
sudo systemctl enable app
echo $(systemctl status app)