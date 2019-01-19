package carmarket.car;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CarsGenerator {

    private static final Random RANDOM = new Random();

    public static List<Car> generate() {
        final List<Car> cars = new ArrayList<>();

        createParameters().forEach(carParameters -> {
            final BodyType bodyType = generateBodyType();
            final FuelType fuelType = generateFuelType();
            final String model = generateModel(carParameters.getBrand());
            final int productionYear = generateProductionYear(carParameters);
            final BigDecimal cost = generateCost(carParameters.getMinCost(), carParameters.getMaxCost());
            final BigDecimal additionalCost = generateCost(carParameters.getMinCost(), carParameters.getMaxCost());

            cars.add(new Car(
                    carParameters.getBrand(),
                    model,
                    bodyType,
                    fuelType,
                    carParameters.getEngineCapacity(),
                    productionYear,
                    cost,
                    additionalCost));
        });
        return cars;
    }

    private static String generateModel(Brand brand) {
        return MessageFormat.format("{0}-{1}", brand ,RANDOM.nextInt() % 100);
    }

    private static FuelType generateFuelType() {
        final FuelType[] fuelTypes = FuelType.values();
        return fuelTypes[RANDOM.nextInt(fuelTypes.length)];
    }

    private static BodyType generateBodyType() {
        final BodyType[] bodyTypes = BodyType.values();
        return bodyTypes[RANDOM.nextInt(bodyTypes.length)];
    }

    private static int generateProductionYear(CarParameters carParameters) {
        return carParameters.getMinProductionYear() +
                RANDOM.nextInt(carParameters.getMaxProductionYear() - carParameters.getMinProductionYear());
    }

    private static BigDecimal generateCost(final BigDecimal min, final BigDecimal max) {
        final BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static List<CarParameters> createParameters() {
        return Arrays.asList(
                new CarParameters(Brand.BMW,
                        1000.0,
                        1990,
                        2000,
                        BigDecimal.valueOf(1800),
                        BigDecimal.valueOf(4200),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(500)),

                new CarParameters(Brand.VOLKSWAGEN,
                        2000.0,
                        1999,
                        2006,
                        BigDecimal.valueOf(4000),
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(1000)),

                new CarParameters(Brand.MERCEDES,
                        1781.0,
                        1998,
                        2006,
                        BigDecimal.valueOf(9000),
                        BigDecimal.valueOf(16000),
                        BigDecimal.valueOf(300),
                        BigDecimal.valueOf(1500)),
                new CarParameters(Brand.AUDI,
                        1281.0,
                        1990,
                        2018,
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(16000),
                        BigDecimal.valueOf(500),
                        BigDecimal.valueOf(1500)),
                new CarParameters(Brand.RENAULT,
                        2281.0,
                        1990,
                        2018,
                        BigDecimal.valueOf(6000),
                        BigDecimal.valueOf(26000),
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(2500))
        );
    }
}
