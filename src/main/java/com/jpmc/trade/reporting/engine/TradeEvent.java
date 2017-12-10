/**
 * Domain model
 */
package com.jpmc.trade.reporting.engine;

import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * @author S4Sushil
 *
 */
public class TradeEvent {

	private final Optional<String> stockName;
	private final Optional<String> buySellIndicator;
	private final Optional<LocalDate> instructionDate;
	private Optional<LocalDate> settlementDate;
	private final Optional<Currency> currency;
	private final Optional<BigDecimal> agreedFx;
	private final Optional<Integer> units;
	private final Optional<BigDecimal> pricePerUnit; 

	private TradeEvent(TradeEvent.TradeEventBuilder builder) {
		this.stockName = builder.stockName;
		this.buySellIndicator = builder.buySellIndicator;
		this.instructionDate = builder.instructionDate;
		this.settlementDate = builder.settlementDate;
		this.currency = builder.currency;
		this.agreedFx = builder.agreedFx;
		this.units = builder.units;
		this.pricePerUnit = builder.pricePerUnit;
	}

	/**
	 * @return the stockName
	 */
	public Optional<String> getStockName() {
		return stockName;
	}

	/**
	 * @return the buySellIndicator
	 */
	public Optional<String> getBuySellIndicator() {
		return buySellIndicator;
	}

	/**
	 * @return the instructionDate
	 */
	public Optional<LocalDate> getInstructionDate() {
		return instructionDate;
	}

	/**
	 * @return the settlementDate
	 */
	public Optional<LocalDate> getSettlementDate() {
		return settlementDate;
	}

	/**
	 * @return the currency
	 */
	public Optional<Currency> getCurrency() {
		return currency;
	}

	/**
	 * @return the agreedFx
	 */
	public Optional<BigDecimal> getAgreedFx() {
		return agreedFx;
	}

	/**
	 * @return the units
	 */
	public Optional<Integer> getUnits() {
		return units;
	}

	/**
	 * @return the pricePerUnit
	 */
	public Optional<BigDecimal> getPricePerUnit() {
		return pricePerUnit;
	}

	/**
	 * Created the mutable method.
	 */
	public void setSettlementDate(Optional<LocalDate> date) {
		this.settlementDate = date;
	}
	
	public static TradeEventBuilder tradeEvent() {
		return new TradeEventBuilder();
	}
	
	public static class TradeEventBuilder {
		private Optional<String> stockName = Optional.empty();
		private Optional<String> buySellIndicator = Optional.empty();
		private Optional<LocalDate> instructionDate = Optional.empty();
		private Optional<LocalDate> settlementDate = Optional.empty();
		private Optional<Currency> currency = Optional.empty();
		private Optional<BigDecimal> agreedFx = Optional.empty();
		private Optional<Integer> units = Optional.empty();
		private Optional<BigDecimal> pricePerUnit = Optional.empty();
		
		/**
		 * @param stockName the stockName to set
		 */
		public TradeEventBuilder setStockName(Optional<String> stockName) {
			this.stockName = stockName;
			return this;
		}
		/**
		 * @param buySellIndicator the buySellIndicator to set
		 */
		public TradeEventBuilder setBuySellIndicator(Optional<String> buySellIndicator) {
			this.buySellIndicator = buySellIndicator;
			return this;
		}
		/**
		 * @param instructionDate the instructionDate to set
		 */
		public TradeEventBuilder setInstructionDate(Optional<LocalDate> instructionDate) {
			this.instructionDate = instructionDate;
			return this;
		}
		/**
		 * @param settlementDate the settlementDate to set
		 */
		public TradeEventBuilder setSettlementDate(Optional<LocalDate> settlementDate) {
			this.settlementDate = settlementDate;
			return this;
		}
		/**
		 * @param currency the currency to set
		 */
		public TradeEventBuilder setCurrency(Optional<Currency> currency) {
			this.currency = currency;
			return this;
		}
		/**
		 * @param agreedFx the agreedFx to set
		 */
		public TradeEventBuilder setAgreedFx(Optional<BigDecimal> agreedFx) {
			this.agreedFx = agreedFx;
			return this;
		}
		/**
		 * @param units the units to set
		 */
		public TradeEventBuilder setUnits(Optional<Integer> units) {
			this.units = units;
			return this;
		}
		/**
		 * @param pricePerUnit the pricePerUnit to set
		 */
		public TradeEventBuilder setPricePerUnit(Optional<BigDecimal> pricePerUnit) {
			this.pricePerUnit = pricePerUnit;
			return this;
			
		}
		
		/**
		 * build trade event Object
		 */
		public TradeEvent build() {
			return new TradeEvent(this);
		}

	}
	
	//TODO: Can move this to abstract Pojo Class.

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return reflectionHashCode(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(obj, this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
