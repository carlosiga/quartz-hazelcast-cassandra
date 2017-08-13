package com.example.demo;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SchedulerController {

    private final CvrSchedulerService schedulerService;

    @Autowired
    public SchedulerController(CvrSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PutMapping("/cvr")
    public void startCvr() throws SchedulerException {
        schedulerService.scheduleCvrJob();
    }


    @DeleteMapping("/cvr")
    public void stopCvr() throws SchedulerException {
        schedulerService.stopCvrJob();
    }
}
