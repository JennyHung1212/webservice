#!/bin/bash
sudo systemctl stop app
sudo chmod +x webservice-0.0.1-SNAPSHOT.jar

# cleanup log file
sudo rm -rf /var/log/niginx/*.log