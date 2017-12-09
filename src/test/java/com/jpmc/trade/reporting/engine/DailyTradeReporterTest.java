package com.jpmc.trade.reporting.engine;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;

/**
 * Unit test for DailyTradeReporter App.
 */
@RunWith(JUnitParamsRunner.class)
public class DailyTradeReporterTest {
	
	@Test
	public void dailyTotalAmount() {
		
		BigDecimal actualAmount = DailyTradeReporter.dailyTotalAmount();
		assertThat(actualAmount, is(BigDecimal.ONE));
	}
}
