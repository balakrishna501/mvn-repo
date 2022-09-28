package com.currypoint.scheduler.luncher;


import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class BRJobLuncher extends QuartzJobBean {


	private static final Logger LOGGER = LoggerFactory.getLogger(BRJobLuncher.class);
	
	private static final String JOB_NAME = "jobName";
	private static final String JOB_KEY = "jobKey";
	private static final String RUN_DATE= "runDate";
	private String jobName;

	private JobLauncher jobLauncher;

	private JobLocator jobLocator;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public JobLauncher getJobLauncher() {
		return jobLauncher;
	}

	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	public JobLocator getJobLocator() {
		return jobLocator;
	}

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try
        {
			Map<String, Object> jobDataMap = context.getMergedJobDataMap();
			String jobName = (String) jobDataMap.get(JOB_NAME);		
			LOGGER.info("Entered into job execution with job execution context: {}", jobName);
            Job job = jobLocator.getJob(jobName);
            JobParameters params = getJobParametersFromJobMap(jobDataMap);
            jobLauncher.run(job, params);
        } 
        catch (Exception e) 
        {
        	LOGGER.error("Exception occred {}",e);
        }
	}
	private JobParameters getJobParametersFromJobMap(Map<String, Object> jobDataMap) {

		JobParametersBuilder builder = new JobParametersBuilder();

		for (Entry<String, Object> entry : jobDataMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String && !key.equals(JOB_NAME)) {
				builder.addString(key, (String) value);
			} else if (value instanceof Float || value instanceof Double) {
				builder.addDouble(key, ((Number) value).doubleValue());
			} else if (value instanceof Integer || value instanceof Long) {
				builder.addLong(key, ((Number) value).longValue());
			} else if (value instanceof Date) {
				builder.addDate(key, (Date) value);
			} else {
				// JobDataMap contains values which are not job parameters
				// (ignoring)
			}
		}
		builder.addDate(RUN_DATE, new Date());
		builder.addString(JOB_KEY, UUID.randomUUID().toString());
		return builder.toJobParameters();

	}
}
