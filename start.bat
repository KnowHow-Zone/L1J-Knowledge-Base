@echo on & color 0B

@@java -Xms1g -Xmx8g -XX:-UseParallelGC -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC -cp l1jserver.jar;lib\c3p0-0.9.1.2.jar;lib\commons-lang-2.6.jar;lib\gson-2.8.4.jar;lib\javolution.jar;lib\jaxb-api-2.3.1.jar;lib\json-20180813.jar;lib\mina-core-2.0.9.jar;lib\mysql-connector-java-5.1.7-bin.jar;lib\netty-all-4.1.31.Final.jar;lib\slf4j-simple-1.7.21.jar;lib\slf4j-api-1.7.21.jar -Dcom.sun.management.jmxremote l1j.server.Server
@pause