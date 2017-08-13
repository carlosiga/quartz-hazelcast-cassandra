package com.example.demo;

import com.bikeemotion.quartz.jobstore.hazelcast.HazelcastJobStore;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.config.Config;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration(exclude = HazelcastAutoConfiguration.class)
public class SchedulerConfiguration {


    @Autowired
    private Environment environment;

    @Bean
    public HazelcastInstance hazelcastInstance(HazelcastMapStore<EntryEntity> hazelcastMapStore) {
        Config config = new Config();

        String groupName = environment.getProperty("group.name", "scheduler-demo");
        String groupPassword = environment.getProperty("group.password", "password");
        String managementCenterUrl = environment.getProperty("managementCenter.url");

        config.getGroupConfig().setName(groupName);
        config.getGroupConfig().setPassword(groupPassword);

        config.setProperty("hazelcast.logging.type", "slf4j");

        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        if (managementCenterUrl != null && !managementCenterUrl.trim().isEmpty()) {
            managementCenterConfig.setEnabled(true);
            managementCenterConfig.setUrl(managementCenterUrl);
        }
        else {
            managementCenterConfig.setEnabled(false);
        }

        config.setManagementCenterConfig(managementCenterConfig);

        return Hazelcast.newHazelcastInstance(config);

    }

    @Bean
    public HazelcastMapStore<EntryEntity> hazelcastMapStore(EntryRepository entryRepository) {
        HazelcastMapStore<EntryEntity> hazelcastMapStore = new HazelcastMapStore<EntryEntity>(entryRepository, EntryEntity.class);
        return hazelcastMapStore;
    }

    @Bean
    public Scheduler scheduler(HazelcastInstance hazelcastInstance,
            HazelcastMapStore hazelcastMapStore) throws SchedulerException {
        // Setting Hazelcast Instance
        HazelcastJobStore.setHazelcastClient(hazelcastInstance);

        // Setting Hazelcast Job Store
        Properties props = new Properties();
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "scheduler-demo");
        props.setProperty(StdSchedulerFactory.PROP_SCHED_JMX_EXPORT, "true");
        props.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, HazelcastJobStore.class.getName());
        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_PREFIX + ".threadCount", "1");
//        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_PREFIX + ".threadPriority", "5");

        Map<String,Class> mapStore = ImmutableMap.<String, Class>builder()
            .put("job-store-map-job", JobKey.class)
            .put("job-store-map-job-by-group-map", JobKey.class)
            .put("job-store-trigger-by-key-map", JobKey.class)
            .put("job-trigger-key-by-group-map", JobKey.class)
            .put("job-paused-trigger-groups", JobKey.class)
            .put("job-paused-job-groups", JobKey.class)
            .put("job-calendar-map", JobKey.class)
            .build();

        // TODO: this is breaking down and I'm running out of time.
        // we need to map the different Quartz entities to their own tables... either by making them parseable
        // via Jackson into JSON or making the tables match what the individual Quartz classes look like.

        // Uncomment these lines which will set up each Cassandra-HazelcastMapStore and you will see what I mean

        // mapStore.entrySet().stream().forEach(mapStoreEntry -> {
        //    addMapStore(hazelcastInstance, hazelcastMapStore, mapStoreEntry.getKey(), mapStoreEntry.getValue());
        // });



        Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
        // Starting Scheduler
        scheduler.start();
        return scheduler;

    }

    private void addMapStore(final HazelcastInstance instance,
                             final HazelcastMapStore hazelcastMapStore,
                             final String mapStoreName,
                             final Class mapStoreClass) {

        Config config = instance.getConfig();

        // Adding mapstore
        final MapConfig mapCfg = config.getMapConfig(mapStoreName);

        final MapStoreConfig mapStoreCfg = new MapStoreConfig();
        mapStoreCfg.setImplementation(hazelcastMapStore);
        mapStoreCfg.setWriteDelaySeconds(1);
        // to load all map at same time
        mapStoreCfg.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);
        mapCfg.setMapStoreConfig(mapStoreCfg);
        config.addMapConfig(mapCfg);

    }

    @Bean
    public JobDetail cvrJob() {
        return JobBuilder.newJob(CvrInterval.class)
                .withIdentity(CvrInterval.CVR_JOB_NAME, CvrInterval.GROUP_NAME)
                .storeDurably()
                .build();
    }

    // TODO: figure out how to do this
//    @PreDestroy
//    public void shutdown() throws SchedulerException {
//        hazelcastInstance.shutdown();
//        scheduler.shutdown();
//        Hazelcast.shutdownAll();
//    }

}
