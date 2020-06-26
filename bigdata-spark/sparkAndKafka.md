

##  kafka and spark streaming


### 规划：
1.  node01:
2.  node02:zk   broker
3.  node03:zk   broker
4.  node04:zk   broker

```
broker.id=
log.dirs=/var/bigdata/kafka
zookeeper.connect=ode02:2181,node03:2181,node04:2181/kafka

kafka-server-start.sh -daemon  $KAFKA_HOME/config/server.properties

kafka-topics.sh   --zookeeper node02:2181,node03:2181,node04:2181/kafka   --create "ooxx"  --partitions 3 --repl
ication-factor  2 

kafka-topics.sh   --zookeeper node02:2181,node03:2181,node04:2181/kafka --list
kafka-topics.sh   --zookeeper node02:2181,node03:2181,node04:2181/kafka --descibe --topic ooxx

kafka-console-producer.sh --broker-list  node04:9092   --topic  ooxx
kafka-console-consumer.sh   --new-consumer --bootstrap-server  node02:9092  --topic  ooxx


kafka-consumer-groups.sh    --new-consumer --bootstrap-server node03:9092  --describe --group 'console-consumer-16806'


__consumer_offsets   consumer@offset@metadata  

alias  kg='kafka-consumer-groups.sh  --new-consumer  --bootstrap-server node02:9092'


GROUP    TOPIC     PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             OWNER
bula1    ooxx      0          4933            4933            0               consumer-1_/192.168.150.1
bula1    ooxx      1          4936            4936            0               consumer-1_/192.168.150.1
bula1    ooxx      2          4933            4933            0               consumer-1_/192.168.150.1
```