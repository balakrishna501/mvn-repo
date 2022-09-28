package com.currypoint.scheduler;

import brave.sampler.Sampler;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SchedulerServiceApplication
{
//	@Autowired
//	JobLauncher jobLauncher;
//
//	@Autowired
//	Job job;
//	@Autowired
//	private JobRegistry jobRegistry;

	public static void main(String[] args) {
		SpringApplication.run(SchedulerServiceApplication.class, args);
	}

//	public void run() throws Exception {
//		Job job = jobRegistry.getJob("processSettlementsJob");
//		JobParameters params = new JobParametersBuilder()
//				.addString("JobID", String.valueOf(System.currentTimeMillis()))
//				.toJobParameters();
//		jobLauncher.run(job, params);
//	}
@Bean
public Sampler defaultSampler() {
	return Sampler.ALWAYS_SAMPLE;
}
}
