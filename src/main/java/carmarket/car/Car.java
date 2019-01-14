package carmarket.car;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Car implements Serializable {

	private Brand brand;
	private String model;
	private BodyType bodyType;
	private FuelType fuelType;
	private Double engineCapacity;
	private Integer productionYear;
	private BigDecimal cost;
	private BigDecimal additionalCost;
}
