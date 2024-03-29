# webservice
CSYE6225 Network Structures and Cloud Computing assignment

## Maven
### Run the project ###
```bash
./mvnw spring-boot:run
```

### Run unit tests ###
```bash
./mvnw test
```

### Install maven packages ###
Clears the target directory and builds the project described by your Maven POM file and installs the resulting artifact (JAR) into your local Maven repository
```bash
./mvnw clean install
```

### Export environment variables ###
```bash
export S3_BUCKET_NAME=99c70780-9fd9-11ec-b5cf-12d3f012b863.dev.jenny-hung.me
export AWS_PROFILE=dev
export AWS_REGION=us-east-1
```

## Packer
```bash
cd src/main/java/edu/neu/coe/csye6225/webapp/ami/
packer validate ami.json
# for dev
# set log mode to verbose
export PACKER_LOG=1 && packer build -var-file='vars_dev.json' ami.json
# for demo
packer build -var-file='vars_demo.json' ami.json
```

## MySQL DB
```bash
mysql -u csye6225 -p -h csye6225.cakj6kkp0h27.us-east-1.rds.amazonaws.com
```