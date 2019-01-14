package carmarket.car;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomCarsGenerator {

    public static List<Car> generateCars() {
        final List<Car> cars = new ArrayList<>();
        final Random random = new Random();
        final BodyType[] bodyTypes = BodyType.values();
        final FuelType[] fuelTypes = FuelType.values();

        createCars().forEach(carModel -> {
            final BodyType bodyType = bodyTypes[random.nextInt(bodyTypes.length)];
            final FuelType fuelType = fuelTypes[random.nextInt(fuelTypes.length)];
            final int productionYear = carModel.getMinProductionYear() + random
                    .nextInt(carModel.getMaxProductionYear() - carModel.getMinProductionYear());
            final BigDecimal cost = generateCost(carModel.getMinCost(), carModel.getMaxCost());
            final BigDecimal additionalCost = generateCost(carModel.getMinCost(), carModel.getMaxCost());

            cars.add(new Car(carModel.getBrand(), carModel.getModel(), bodyType,
                    fuelType, carModel.getEngineCapacity(), productionYear,
                    cost, additionalCost));
        });
        return cars;
    }

    private static List<CarModel> createCars() {
        return Arrays.asList(new CarModel(Brand.BMW, "Cinquecento",
                704.0, 1991, 1998, BigDecimal.valueOf(1800), BigDecimal.valueOf(4200),
                BigDecimal.valueOf(0), BigDecimal.valueOf(500)),

                new CarModel(Brand.VOLKSWAGEN, "Golf IV",
                        1595.0, 1999, 2006, BigDecimal.valueOf(4000),
                        BigDecimal.valueOf(8000),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(1000)),

                new CarModel(Brand.MERCEDES, "TT 8N",
                        1781.0, 1998, 2006, BigDecimal.valueOf(9000),
                        BigDecimal.valueOf(16000),
                        BigDecimal.valueOf(300), BigDecimal.valueOf(1500)),

                new CarModel(Brand.BMW, "PANDA II",
                        1242.0, 2003, 2012, BigDecimal.valueOf(5500),
                        BigDecimal.valueOf(12000),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(950)),
                new CarModel(Brand.VOLKSWAGEN, "Polo IV",
                        1896.0, 2001, 2005, BigDecimal.valueOf(4700),
                        BigDecimal.valueOf(15000),
                        BigDecimal.valueOf(750), BigDecimal.valueOf(2300)),
                new CarModel(Brand.MERCEDES, "A6 C6",
                        2461.0, 2004, 2011, BigDecimal.valueOf(13000),
                        BigDecimal.valueOf(25000),
                        BigDecimal.valueOf(1200), BigDecimal.valueOf(5000)),
                new CarModel(Brand.BMW, "Grande Punto",
                        1910.0, 2005, 2009, BigDecimal.valueOf(8700),
                        BigDecimal.valueOf(18000),
                        BigDecimal.valueOf(400), BigDecimal.valueOf(1900)),
                new CarModel(Brand.VOLKSWAGEN, "Touareg I",
                        3598.0, 2002, 2010, BigDecimal.valueOf(17000),
                        BigDecimal.valueOf(32000),
                        BigDecimal.valueOf(800), BigDecimal.valueOf(3500))
                );
    }


    private static BigDecimal generateCost(final BigDecimal min, final BigDecimal max) {
        final BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
