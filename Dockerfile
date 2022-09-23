FROM docker.boulderaitech.com/boulder-docker-local/jdk:1.8_py
ENV TZ=CST-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD dbswitch-admin/target/dbswitch-admin-*.jar /app.jar

ENTRYPOINT ["sh","-c","java -jar -Dfile.encoding=UTF-8 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap /app.jar --server.port=9088"]
