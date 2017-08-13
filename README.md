# Scheduler Demo
This is a Spring Boot project that incorporates the HazelcastJobStore for use with the Quartz scheduling library.

You can build into docker containers in order to test distributed behavior of the scheduler.

You should be able to run 

```$bash
docker-compose up
```

The scheduler services will fail the first time because no keyspace has been set up.

### Warning!

This implementation is half-finished.  The Quartz jobs will be persisted into a Hazelcast Job Store.  Durability is turned on so your jobs, triggers, etc. will live as long as the cluster is alive.

However, once you bring down the entire cluster, you lose your jobs... this can be fixed by backing each Job store with the HazelcastMapStore construct, delegating to a DAO that will store the data element into Cassandra.

See the comments in SchedulerConfiguration on what work is left on this.


## Setup Keyspace and Table

Run the following command to create the keyspace.  Because the volume has been mapped out of the container, you 
should only have to do this once.

From a different terminal than the one running your docker-compose, run

```$bash
>docker exec -it a599ab2885e3 /bin/bash
root@a599ab2885e3:/# cqlsh
Connected to Test Cluster at localhost:9160.
[cqlsh 4.1.1 | Cassandra 2.0.10 | CQL spec 3.1.1 | Thrift protocol 19.39.0]
Use HELP for help.
cqlsh> create keyspace scheduler with replication = { 'class' : 'SimpleStrategy', 'replication_factor': 1 };
cqlsh> exit
root@a599ab2885e3:/# cqlsh -k scheduler
Connected to Test Cluster at localhost:9160.
[cqlsh 4.1.1 | Cassandra 2.0.10 | CQL spec 3.1.1 | Thrift protocol 19.39.0]
Use HELP for help.
cqlsh:scheduler> create table if not exists scheduler.HzEntry (
             ...   id text PRIMARY KEY,
             ...   data text
             ... );
root@a599ab2885e3:/# exit
exit
```

Be sure to replace ``a599ab2885e3`` with your container id (can be obtained from ``docker ps``).

These CQL commands can be found in ``src/main/resources`` if you would like to copy them.
## Beware startup order
Now you can stop your docker-compose and bring it back up.  The next time everything should start up 
(provided Cassandra gets booted before the services try to connect to it).

You can restart a particular service by doing
``docker-compose restart scheduler-1``

Note: The best way to do this is introduce a ``wait-for.sh`` script, check the Docker documentation for more info.