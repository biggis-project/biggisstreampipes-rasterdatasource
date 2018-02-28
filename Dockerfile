FROM anapsix/alpine-java

EXPOSE 9000

ADD ./target/scala-2.12/rasterdata-source.jar  /rasterdata-source.jar

WORKDIR /

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /rasterdata-source.jar"]
