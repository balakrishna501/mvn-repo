package com.currypoint.scheduler.processor;

import com.currypoint.scheduler.modal.Settlement;
import org.springframework.batch.item.ItemProcessor;

public class SettlementProcessor implements ItemProcessor<Settlement, Settlement>{

	@Override
	public Settlement process(Settlement item) throws Exception {
		
		return item;
	}

}
