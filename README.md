# ClamAV with SpringBoot Demo App

#### ClamAV is an open source Antivirus toolkit with a number of utilities including a flexible and scalable multi-threaded daemon, a command line scanner and advanced tool for automatic database update.


Here we show how to remotely scan a file using clamAV running on a docker container on port 3310

#### We assume ClamAV is running on a container with configs as below
```
docker run --hostname=clamav --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin --env=TZ=Etc/UTC -p 3310:3310 -p 7357:7357 --label='maintainer=ClamAV bugs <clamav-bugs@external.cisco.com>' --runtime=runc -d clamav/clamav:latest
```
