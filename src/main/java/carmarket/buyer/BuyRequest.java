package carmarket.buyer;

import carmarket.car.BodyType;
import carmarket.car.Brand;
import carmarket.car.FuelType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Builder
@ToString
public class BuyRequest implements Serializable {

	@Builder.Default private List<Brand> brands = new ArrayList<>();
	@Builder.Default private List<String> models = new ArrayList<>();
	@Builder.Default private List<FuelType> fuelTypes = new ArrayList<>();
	@Builder.Default private List<BodyType> bodyTypes = new ArrayList<>();
	@Builder.Default private Double minEngineCapacity = 0d;
	@Builder.Default private Double maxEngineCapacity = Double.MAX_VALUE;
	@Builder.Default private Integer minProductionYear = 1_500;
	@Builder.Default private Integer maxProductionYear = 2_020;
	@Builder.Default private BigDecimal minCost = BigDecimal.ZERO;
	@Builder.Default private BigDecimal maxCost = BigDecimal.valueOf(Double.MAX_VALUE);
	@Builder.Default private BigDecimal minAdditionalCost = BigDecimal.ZERO;
	@Builder.Default private BigDecimal maxAdditionalCost = BigDecimal.valueOf(Double.MAX_VALUE);

	@Setter
	private boolean processing;
}
