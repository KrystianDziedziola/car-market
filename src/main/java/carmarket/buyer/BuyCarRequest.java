package carmarket.buyer;

import carmarket.car.BodyType;
import carmarket.car.Brand;
import carmarket.car.FuelType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor()
@EqualsAndHashCode
@Getter
@Builder
@ToString
public class BuyCarRequest implements Serializable {

	private List<Brand> brands;
	private List<String> models;
	private List<FuelType> fuelTypes;
	private List<BodyType> bodyTypes;
	private Double minEngineCapacity;
	private Double maxEngineCapacity;
	private Integer minProductionYear;
	private Integer maxProductionYear;
	private BigDecimal minCost;
	private BigDecimal maxCost;
	private BigDecimal minAdditionalCost;
	private BigDecimal maxAdditionalCost;
	@Setter
	private boolean processing;
}
