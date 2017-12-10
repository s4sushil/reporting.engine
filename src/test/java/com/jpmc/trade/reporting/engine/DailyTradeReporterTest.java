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
		
		//Generates the daily amount report
		System.out.println("");
		System.out.println(" *** Generates Total Amount Report *** ");
		DailyTradeReporter.generateTotalAmountReport(tradeEvents, buySellIndicator);
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
	
	@Test
	@Parameters(source = TradeEventRankProvider.class)
	public void rankingByDate(TradeEvent t1, TradeEvent t2, TradeEvent t3, LocalDate settlementDayWednesday,  
								 String buySellIndicator, String expectedResult) {

		List<TradeEvent> tradeEvents = ImmutableList.of(t1, t2, t3);
		Map<LocalDate, String> actualAmount = DailyTradeReporter.findRankingGroupByDate(tradeEvents, buySellIndicator);
		assertThat(actualAmount.get(settlementDayWednesday), is(expectedResult));

		//Generates the daily rank report
		System.out.println("");
		System.out.println(" *** Generates Top Rankers Report *** ");
		DailyTradeReporter.generateRankingReport(tradeEvents, buySellIndicator);
	}
	
	public static class TradeEventRankProvider {
		public static Object[][] provideRanking() {
            LocalDate wednesday = LocalDate.of(2017, 12, 06);
			
            TradeEvent buyTradeAmt500 = createTradeEvent(Optional.of("JPMC"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(wednesday), Optional.of(5));
            
            TradeEvent buyTradeAmt1000 = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(wednesday), Optional.of(10));
            
            TradeEvent buyTradeAmt1500 = createTradeEvent(Optional.of("Apple"), Optional.of(Currency.getInstance("SAR")),
            		Optional.of("B"), Optional.of(wednesday), Optional.of(15));

            TradeEvent actionEventEmpty = createTradeEvent(Optional.of("None"), Optional.of(Currency.getInstance("SAR")),
            		Optional.empty(), Optional.of(wednesday), Optional.of(5));
            
            TradeEvent numberOfUnitEmpty = createTradeEvent(Optional.of("Other"), Optional.of(Currency.getInstance("AED")),
            		Optional.of("B"), Optional.of(wednesday), Optional.empty());
                        
            TradeEvent sellTradeAmt5000 = createTradeEvent(Optional.of("JPMC"), Optional.of(Currency.getInstance("USD")),
            		Optional.of("S"), Optional.of(wednesday), Optional.of(50));

            TradeEvent sellTradeAmt2000 = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("GBP")),
            		Optional.of("S"), Optional.of(wednesday), Optional.of(20));

            TradeEvent sellTradeAmt3000 = createTradeEvent(Optional.of("MS"), Optional.of(Currency.getInstance("INR")),
            		Optional.of("S"), Optional.of(wednesday), Optional.of(30));
            
			return new Object[][] {
				//Passing 3 Arab currency BUY trades for wed hence settlementDate will be same day. 
				//Action filter is Buy so, highest amt is 1500 for entity Apple.
            	{buyTradeAmt500, buyTradeAmt1000, buyTradeAmt1500, wednesday, "B", "Apple"},
            	
            	//Passing 3 Arab currency BUY trades for wed hence settlementDate will be same day. 
            	//Action filter is Sell hence expected nothing.
            	{buyTradeAmt500, buyTradeAmt1000, buyTradeAmt1500, wednesday, "S", null},

            	//Passing 2 Arab currency BUY trades and 1 Optional empty Action for wed hence settlementDate will be same day.
            	//Indicator is Buy and highest total is 1000 for entity MS
            	{buyTradeAmt500, buyTradeAmt1000, actionEventEmpty, wednesday, "B", "MS"},

            	//Passing 2 Arab currency BUY trades and 1 Optional empty Action for wed hence settlementDate will be same day.
            	//Indicator is Buy(ignore case) and highest total is 1000 for entity MS
            	{buyTradeAmt500, buyTradeAmt1000, actionEventEmpty, wednesday, "b", "MS"},
            	
            	//Passing 1 Arab currency BUY trades, 1 empty number of unit and 1 Optional empty Action. 
            	//Indicator is Buy and highest total is 500 for entity MS
            	{buyTradeAmt500, numberOfUnitEmpty, actionEventEmpty, wednesday, "B", "JPMC"},
            	
				//Passing 3 Non Arab currency SELL trades for wed hence settlementDate will be same day..
            	//Action filter is Sell and highest total is 5000 for entity MS
            	{sellTradeAmt5000, sellTradeAmt2000, sellTradeAmt3000, wednesday, "S", "JPMC"},

				//Passing 3 Non Arab currency SELL trades for wed hence settlementDate will be same day..
            	//Action filter is BUY so, nothing expected.
            	{sellTradeAmt5000, sellTradeAmt2000, sellTradeAmt3000, wednesday, "B", null},
            	
				//Passing 1 Non Arab currency SELL trades, 1 empty number of unit and 1 Optional empty Action.
            	//Action filter is Sell and highest total is 5000 for entity MS
            	{sellTradeAmt5000, numberOfUnitEmpty, actionEventEmpty, wednesday, "S", "JPMC"},
            	
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