package carmarket.car;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
class CarModel {

	private Brand brand;
	private String model;
	private Double engineCapacity;
	private Integer minProductionYear;
	private Integer maxProductionYear;
	private BigDecimal minCost;
	private BigDecimal maxCost;
	private BigDecimal minAdditionalCost;
	private BigDecimal maxAdditionalCost;
}
