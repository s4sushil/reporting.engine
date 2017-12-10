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
				.forEach((date, list) -> 
						groupByDate.put(date.get(), list.stream()
							.map(tradeEvent -> tradeEvent.getAgreedFx().orElse(BigDecimal.ZERO)
									.multiply(BigDecimal.valueOf(tradeEvent.getUnits().orElse(0)))
									.multiply(tradeEvent.getPricePerUnit().orElse(BigDecimal.ZERO)))
							.reduce(BigDecimal.ZERO, BigDecimal::add))
						);
		
		return groupByDate;
	}

	public static void generateTotalAmountReport(List<TradeEvent> tradeEvents, String buySellIndicator) {
		System.out.println("List of total amount listed below for Action: " + buySellIndicator);
		dailyTotalAmountGroupByDate(tradeEvents, buySellIndicator).forEach((date, amt) -> {
			System.out.println(" on Date: " + date + " total amt <" + amt + ">");
		});
	}

	public static Map<LocalDate, String> findRankingGroupByDate(List<TradeEvent> tradeEvents, String buySellIndicator) {
		Map<LocalDate, String> rankByDate = new HashMap<>();
		List<TradeEvent> updatedTradeEvents = filterAndUpdateSettlementDates(tradeEvents, buySellIndicator);
		
		updatedTradeEvents.stream()
				.collect(Collectors.groupingBy(TradeEvent::getSettlementDate, Collectors.toList()))
				.forEach((date, list) -> rankByDate
						.put(date.get(), list.stream()
							.sorted((tradeEvent1, tradeEvent2) -> tradeEvent2.getAgreedFx().orElse(BigDecimal.ZERO)
									.multiply(BigDecimal.valueOf(tradeEvent2.getUnits().orElse(0)))
									.multiply(tradeEvent2.getPricePerUnit().orElse(BigDecimal.ZERO))
							.compareTo(tradeEvent1.getAgreedFx().orElse(BigDecimal.ZERO)
									.multiply(BigDecimal.valueOf(tradeEvent1.getUnits().orElse(0)))
									.multiply(tradeEvent1.getPricePerUnit().orElse(BigDecimal.ZERO))))
							.findFirst().map(event -> event.getStockName().orElse("")).orElse("")));
		return rankByDate;
	}
	
	public static void generateRankingReport(List<TradeEvent> tradeEvents, String buySellIndicator) {
		System.out.println("Top Stock Name for Action: " + buySellIndicator);
		findRankingGroupByDate(tradeEvents, buySellIndicator).forEach((date, entity) -> {
			System.out.println(" on Date: " + date + " topper is stock <" + entity +">");
		});
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
