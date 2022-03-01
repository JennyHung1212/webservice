#!/bin/sh
export AWS_ACCESS_KEY_ID=AKIAYIPUDCCRDFCNZPHM
export AWS_SECRET_ACCESS_KEY=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
export AWS_REGION=us-east-1
aws s3 cp s3://csye6225-webservice-artifact/build/webservice-0.0.1-SNAPSHOT.jar .

sleep 10
current_dir=$(pwd)
sudo chmod +x "$current_dir"/webservice-0.0.1-SNAPSHOT.jar
#sudo java -jar webservice-0.0.1-SNAPSHOT.jar
