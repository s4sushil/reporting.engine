package com.jpmc.trade.reporting.engine;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.jpmc.trade.reporting.engine.TradeEvent.TradeEventBuilder;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Unit test for DailyTradeReporter.
 */
@RunWith(JUnitParamsRunner.class)
public class DailyTradeReporterTest {

	@Test
	@Parameters(source = TradeEventProvider.class)
	public void dailyTotalAmount(TradeEvent t1, TradeEvent t2, TradeEvent t3, LocalDate updatedSettlementToWeekDay,  
								 String buySellIndicator, BigDecimal expectedResult) {

		List<TradeEvent> tradeEvents = ImmutableList.of(t1, t2, t3);
		Map<LocalDate, BigDecimal> actualAmount = DailyTradeReporter.dailyTotalAmountGroupByDate(tradeEvents, buySellIndicator);
		assertThat(actualAmount.get(updatedSettlementToWeekDay), is(expectedResult));
	}

	public static class TradeEventProvider {
		
		public static Object[][] provideTradeEvent() {
            LocalDate friday = LocalDate.of(2017, 12, 01);
			
            TradeEvent buyTradeArabCurrencyOnWeekend = createTradeEvent(Optional.of("JPMC"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(friday), Optional.of(5));
            
            TradeEvent buyTradeArabCurrencyOnSameWeekend = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(friday), Optional.of(5));
            
            TradeEvent buyTradeArab2CurrencyOnSameWeekend = createTradeEvent(Optional.of("Apple"), Optional.of(Currency.getInstance("SAR")),
            		Optional.of("B"), Optional.of(friday), Optional.of(5));

            TradeEvent actionEventEmpty = createTradeEvent(Optional.of("None"), Optional.of(Currency.getInstance("SAR")),
            		Optional.empty(), Optional.of(friday), Optional.of(5));
            
            TradeEvent numberOfUnitEmpty = createTradeEvent(Optional.of("Other"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(friday), Optional.empty());
                        
            TradeEvent sellTradeNotWeekendUSD = createTradeEvent(Optional.of("JPMC"), Optional.of(Currency.getInstance("USD")),
            		Optional.of("S"), Optional.of(friday), Optional.of(10));

            TradeEvent sellTradeNotWeekendGBP = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("GBP")),
            		Optional.of("S"), Optional.of(friday), Optional.of(10));

            TradeEvent sellTradeNotWeekendINR = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("INR")),
            		Optional.of("S"), Optional.of(friday), Optional.of(10));
            
			return new Object[][] {
				//Passing 3 Arab currency BUY trades for friday hence settlementDate will be Sunday. 
				//Action filter is Buy so, 500 * 3 = 1500 is expected totalAmt.
            	{buyTradeArabCurrencyOnWeekend, buyTradeArabCurrencyOnSameWeekend, buyTradeArab2CurrencyOnSameWeekend, friday.plusDays(2), "B", BigDecimal.valueOf(1500)},
            	
            	//Passing 3 Arab currency BUY trades for friday hence settlementDate will be Sunday. 
            	//Action filter is Sell hence expected nothing.
            	{buyTradeArabCurrencyOnWeekend, buyTradeArabCurrencyOnSameWeekend, buyTradeArab2CurrencyOnSameWeekend, friday.plusDays(2), "S", null},

            	//Passing 2 Arab currency BUY trades for friday and 1 Optional empty Action.
            	//Indicator is Buy so, 500 * 2 = 1000 is expected totalAmt.
            	{buyTradeArabCurrencyOnWeekend, buyTradeArabCurrencyOnSameWeekend, actionEventEmpty, friday.plusDays(2), "B", BigDecimal.valueOf(1000)},

            	//Passing 2 Arab currency BUY trades for friday and 1 Optional empty Action.
            	//Indicator is Buy(ignore case) so, 500 * 2 = 1000 is expected.
            	{buyTradeArabCurrencyOnWeekend, buyTradeArabCurrencyOnSameWeekend, actionEventEmpty, friday.plusDays(2), "B", BigDecimal.valueOf(1000)},
            	
            	//Passing 1 Arab currency BUY trades, 1 empty number of unit and 1 Optional empty Action. 
            	//Indicator is Buy so, 500 * 1 = 500 is expected totalAmt.
            	{buyTradeArabCurrencyOnWeekend, numberOfUnitEmpty, actionEventEmpty, friday.plusDays(2), "B", BigDecimal.valueOf(500)},
            	
				//Passing 3 Non Arab currency SELL trades for friday hence settlementDate will be Friday.
            	//Action filter is Sell so, 1000 * 3 = 3000 is expected totalAmt.
            	{sellTradeNotWeekendUSD, sellTradeNotWeekendGBP, sellTradeNotWeekendINR, friday, "S", BigDecimal.valueOf(3000)},

				//Passing 3 Non Arab currency SELL trades for friday hence settlementDate will be Friday.
            	//Action filter is BUY so, nothing expected.
            	{sellTradeNotWeekendUSD, sellTradeNotWeekendGBP, sellTradeNotWeekendINR, friday, "B", null},
            	
				//Passing 1 Non Arab currency SELL trades, 1 empty number of unit and 1 Optional empty Action.
            	//Action filter is Sell so, 1000 * 1 = 1000 is expected totalAmt.
            	{sellTradeNotWeekendUSD, numberOfUnitEmpty, actionEventEmpty, friday, "S", BigDecimal.valueOf(1000)}
            	
            };
		}
	}
	
	private static TradeEvent createTradeEvent(Optional<String> entity, Optional<Currency> currency, Optional<String> buySell,
											   Optional<LocalDate> originalSettlementDate, Optional<Integer> units) {
		
		TradeEventBuilder builder = new TradeEventBuilder();
		builder.setStockName(entity);
		builder.setCurrency(currency);
		builder.setBuySellIndicator(buySell);
		builder.setSettlementDate(originalSettlementDate);
		builder.setUnits(units);
		//setting default to 10 * 10 = 100 * units
		builder.setPricePerUnit(Optional.of(BigDecimal.TEN));
		builder.setAgreedFx(Optional.of(BigDecimal.TEN));
		
		return builder.build();
	}
	
}