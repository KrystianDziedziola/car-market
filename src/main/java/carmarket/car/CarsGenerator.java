package carmarket.car;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CarsGenerator {

    private static final Random RANDOM = new Random();

    public static List<Car> generate() {
        final List<Car> cars = new ArrayList<>();

        createCars().forEach(carModel -> {
            final BodyType bodyType = generateBodyType();
            final FuelType fuelType = generateFuelType();
            final int productionYear = generateProductionYear(carModel);
            final BigDecimal cost = generateCost(carModel.getMinCost(), carModel.getMaxCost());
            final BigDecimal additionalCost = generateCost(carModel.getMinCost(), carModel.getMaxCost());

            cars.add(new Car(
                    carModel.getBrand(),
                    carModel.getModel(),
                    bodyType,
                    fuelType,
                    carModel.getEngineCapacity(),
                    productionYear,
                    cost,
                    additionalCost));
        });
        return cars;
    }

    private static FuelType generateFuelType() {
        final FuelType[] fuelTypes = FuelType.values();
        return fuelTypes[RANDOM.nextInt(fuelTypes.length)];
    }

    private static BodyType generateBodyType() {
        final BodyType[] bodyTypes = BodyType.values();
        return bodyTypes[RANDOM.nextInt(bodyTypes.length)];
    }

    private static int generateProductionYear(CarModel carModel) {
        return carModel.getMinProductionYear() +
                RANDOM.nextInt(carModel.getMaxProductionYear() - carModel.getMinProductionYear());
    }

    private static BigDecimal generateCost(final BigDecimal min, final BigDecimal max) {
        final BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static List<CarModel> createCars() {
        return Arrays.asList(new CarModel(Brand.BMW, "BMW-1234",
                        704.0, 1991, 1998, BigDecimal.valueOf(1800), BigDecimal.valueOf(4200),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(500)),

                new CarModel(Brand.VOLKSWAGEN, "VOLKSVAGEN-1234",
                        1595.0, 1999, 2006, BigDecimal.valueOf(4000),
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(1000)),

                new CarModel(Brand.MERCEDES, "MERCEDES-1234",
                        1781.0, 1998, 2006, BigDecimal.valueOf(9000),
                        BigDecimal.valueOf(16000),
                        BigDecimal.valueOf(300), BigDecimal.valueOf(1500))
        );
    }
}
