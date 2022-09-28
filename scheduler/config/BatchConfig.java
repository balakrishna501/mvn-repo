package com.currypoint.scheduler.config;




import javax.persistence.EntityManagerFactory;

import com.currypoint.common.dto.SettlementDTO;
import com.currypoint.scheduler.modal.Settlement;
import com.currypoint.scheduler.writer.SettlementProcessWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableBatchProcessing
public class BatchConfig {


	@Autowired
	private  EntityManagerFactory entityManagerFactory;
	
	@Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
 
    @Autowired
    private SettlementProcessWriter settlementProcessWriter;

    
    @Bean
    public Step prcessSettlement() throws Exception{
        return steps.get("processSettlementStep")
        		.<Settlement,Settlement>chunk(20)
        		.reader(settlementPrecessingReader())
        		.writer(settlementProcessWriter)
                .build();
    }

	@Bean(name="processSettlementsJob")
	public Job settlementJob() throws Exception{
		return jobs.get("processSettlementsJob")
				.incrementer(new RunIdIncrementer())
				.start(prcessSettlement())
				.build();
	}

    public JpaPagingItemReader<Settlement> settlementPrecessingReader() throws Exception {
    	String query="SELECT ID,AMOUNT,ORDER_ID,PICKUP_DATE,RAZORPAY_ORDER_ID,RESTAURANT_ID,STATUS,USER_TYPE FROM SETTLEMENT WHERE PICKUP_DATE > NOW() - INTERVAL 7 DAY";
    	JpaPagingItemReader<Settlement> reader = new JpaPagingItemReader<>();
    	reader.setEntityManagerFactory(entityManagerFactory);		
		JpaNativeQueryProvider<Settlement> jpaQuey=new JpaNativeQueryProvider<Settlement>();
		jpaQuey.setEntityClass(Settlement.class);
		jpaQuey.setSqlQuery(query);		
		reader.setQueryProvider(jpaQuey);
		reader.setPageSize(100);
       	reader.setMaxItemCount(500);
    	reader.afterPropertiesSet();
    	reader.setSaveState(true);
    	return reader;
    }
 }
