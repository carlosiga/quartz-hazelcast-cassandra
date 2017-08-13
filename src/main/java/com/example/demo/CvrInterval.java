package com.example.demo;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CvrInterval implements Job {

    Logger LOG = LoggerFactory.getLogger(CvrInterval.class);

    public static final String GROUP_NAME = "demoGroup";
    public static final String CVR_JOB_NAME = "cvrJob";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("STARTING CVR");
    }
}
