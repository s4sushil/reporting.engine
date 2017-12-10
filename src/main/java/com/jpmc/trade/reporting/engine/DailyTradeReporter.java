package com.jpmc.trade.reporting.engine;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DailyTradeReporter {

	/**
	 * Takes the list of input trades and filters it based on action passed (Outgoing - BUY / Incoming - SELL).
	 * Later updates the settlementDate to the next working day.
	 * 
	 * Calculates the total amount on each working date and stores it in a Map.
	 * 
	 */
	public static Map<LocalDate, BigDecimal> dailyTotalAmountGroupByDate(List<TradeEvent> tradeEvents, String buySellIndicator) {
		
		Map<LocalDate, BigDecimal> groupByDate = new HashMap<>();
		List<TradeEvent> updatedTradeEvents = filterAndUpdateSettlementDates(tradeEvents, buySellIndicator);
		
		updatedTradeEvents.stream()
				.collect(Collectors.groupingBy(TradeEvent::getSettlementDate, Collectors.toList()))
				.forEach((date, set) -> 
						groupByDate.put(date.get(), set.stream()
							.map(tradeEvent -> tradeEvent.getAgreedFx().orElse(BigDecimal.ZERO)
									.multiply(BigDecimal.valueOf(tradeEvent.getUnits().orElse(0)))
									.multiply(tradeEvent.getPricePerUnit().orElse(BigDecimal.ZERO)))
							.reduce(BigDecimal.ZERO, BigDecimal::add))
						);
		return groupByDate;
	}

	private static List<TradeEvent> filterAndUpdateSettlementDates(List<TradeEvent> tradeEvents,
			String buySellIndicator) {

		Predicate<? super TradeEvent> buySellAction = tradeEvent -> tradeEvent.getBuySellIndicator()
				.map(tradeAction -> tradeAction.equalsIgnoreCase(buySellIndicator)).orElse(false);

		List<TradeEvent> updatedTradeEvents = new ArrayList<>();
		tradeEvents.stream().filter(buySellAction)
				.forEach(tradeEvent -> tradeEvent.getSettlementDate().ifPresent(setDate -> {
					updateSettlementDateForWeekends(tradeEvent, setDate);
					updatedTradeEvents.add(tradeEvent);
				}));

		return updatedTradeEvents;

	}

	/**
	 * Finds the next working day based on the currency type
	 */
	private static void updateSettlementDateForWeekends(TradeEvent tradeEvent, LocalDate setDate) {
		DayOfWeek dayOfWeek = setDate.getDayOfWeek();
		boolean isArabicCurrency = tradeEvent.getCurrency()
				.map(currencyType -> "AED".equalsIgnoreCase(currencyType.getCurrencyCode()) 
						|| "SAR".equalsIgnoreCase(currencyType.getCurrencyCode()))
				.orElse(false);
		
		if(isArabicCurrency) {
			if(dayOfWeek == DayOfWeek.FRIDAY) {
				tradeEvent.setSettlementDate(Optional.of(setDate.plusDays(2)));
			} else if(dayOfWeek == DayOfWeek.SATURDAY) {
				tradeEvent.setSettlementDate(Optional.of(setDate.plusDays(1)));
			}
		} else {
			if(dayOfWeek == DayOfWeek.SATURDAY) {
				tradeEvent.setSettlementDate(Optional.of(setDate.plusDays(2)));
			} else if(dayOfWeek == DayOfWeek.SUNDAY) {
				tradeEvent.setSettlementDate(Optional.of(setDate.plusDays(1)));
			}
		}
	}

}
