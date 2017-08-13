package com.example.demo;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CvrSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(CvrSchedulerService.class);

    private final Scheduler scheduler;
    private final JobDetail cvrJob;

    public static final String DEFAULT_CVR_TRIGGER_NAME = "defaultCvrTrigger";

    @Autowired
    public CvrSchedulerService(Scheduler scheduler, JobDetail cvrJob) {
        this.scheduler = scheduler;
        this.cvrJob = cvrJob;
    }

    public void scheduleCvrJob() throws SchedulerException {

        scheduler.addJob(cvrJob, true);
        LOG.info("Added job {}...", cvrJob);

        DefaultTriggerCheck defaultTriggerCheck = new DefaultTriggerCheck().invoke();
        boolean exists = defaultTriggerCheck.isExists();
        TriggerKey defaultCvrTrigger = defaultTriggerCheck.getDefaultCvrTrigger();
        LOG.info("Check for trigger {}: {}", defaultCvrTrigger, exists);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(DEFAULT_CVR_TRIGGER_NAME, cvrJob.getKey().getGroup())
                .forJob(cvrJob)
                .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
//                    .withIntervalInMinutes(15))
                        .withIntervalInSeconds(15))
                .build();


        scheduler.scheduleJob(trigger);
        LOG.info("Scheduled job with trigger {}", trigger);

    }

    public void stopCvrJob() throws SchedulerException {
        DefaultTriggerCheck defaultTriggerCheck = new DefaultTriggerCheck().invoke();
        boolean exists = defaultTriggerCheck.isExists();
        TriggerKey defaultCvrTrigger = defaultTriggerCheck.getDefaultCvrTrigger();

        scheduler.unscheduleJob(defaultCvrTrigger);
        LOG.info("Unscheduled job {}", defaultCvrTrigger);
        scheduler.deleteJob(cvrJob.getKey());
        LOG.info("Deleted job {}", cvrJob);

    }

    private class DefaultTriggerCheck {
        private TriggerKey defaultCvrTrigger;
        private boolean exists;

        public TriggerKey getDefaultCvrTrigger() {
            return defaultCvrTrigger;
        }

        public boolean isExists() {
            return exists;
        }

        public DefaultTriggerCheck invoke() throws SchedulerException {
            defaultCvrTrigger = TriggerKey.triggerKey(DEFAULT_CVR_TRIGGER_NAME, cvrJob.getKey().getGroup());
            exists = scheduler.checkExists(defaultCvrTrigger);
            LOG.debug("{} exists? {}", defaultCvrTrigger, exists);
            return this;
        }
    }
}
