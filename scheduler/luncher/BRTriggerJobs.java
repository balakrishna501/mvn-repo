package com.currypoint.scheduler.luncher;

import java.io.IOException;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class BRTriggerJobs {
	
	    @Autowired
	    private JobLauncher jobLauncher;
	     
	    @Autowired
	    private JobLocator jobLocator;
	    
	    @Value("${CRON.EXP.SETTLEMENT.JOB}")
	    private String settlementJobExp;
	    
	    @Bean
	    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
	        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
	        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
	        return jobRegistryBeanPostProcessor;
	    }
	    
	    public JobDetail settlementJob() {
	    	JobDataMap jobDataMap = new JobDataMap();
	    	jobDataMap.put("jobName", "processSettlementsJob");
	        jobDataMap.put("jobLauncher", jobLauncher);
	        jobDataMap.put("jobLocator", jobLocator);
	        
	        return JobBuilder.newJob(BRJobLuncher.class)
	        		.withIdentity("settlementJob")
	        		.usingJobData(jobDataMap)
	        		.storeDurably()
	        		.build();
	    }


	  public Trigger settlementTrigger() {
		  return TriggerBuilder
				  .newTrigger()
				    .forJob(settlementJob())
				    .withIdentity("settlementTrigger")
				    .withSchedule(CronScheduleBuilder.cronSchedule(settlementJobExp))
				    .build();
	  }

	  
	  @Bean
	    public SchedulerFactoryBean schedulerFactoryBean() throws IOException 
	    {
	        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
	        scheduler.setTriggers(settlementTrigger());
	        scheduler.setSchedulerName("BRJobLuncher");
	      //  scheduler.setQuartzProperties(quartzProperties());
	       scheduler.setJobDetails(settlementJob());
	        return scheduler;
	    } 
	/*
	 * @Bean public Properties quartzProperties() throws IOException {
	 * PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
	 * propertiesFactoryBean.setLocation(new
	 * ClassPathResource("/quartz.properties"));
	 * propertiesFactoryBean.afterPropertiesSet(); return
	 * propertiesFactoryBean.getObject(); }
	 */
	 
	     
}
