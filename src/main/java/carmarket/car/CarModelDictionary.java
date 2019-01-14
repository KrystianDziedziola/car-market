package carmarket.car;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class CarModelDictionary {

	static List<CarModel> CARS = new ArrayList<>();

	static {
		CARS.add(new CarModel(Brand.BMW, "Cinquecento",
			704.0, 1991, 1998, BigDecimal.valueOf(1800), BigDecimal.valueOf(4200),
			BigDecimal.valueOf(0), BigDecimal.valueOf(500)));
		CARS.add(new CarModel(Brand.VOLKSWAGEN, "Golf IV",
			1595.0, 1999, 2006, BigDecimal.valueOf(4000),
			BigDecimal.valueOf(8000),
			BigDecimal.valueOf(0), BigDecimal.valueOf(1000)));
		CARS.add(new CarModel(Brand.MERCEDES, "TT 8N",
			1781.0, 1998, 2006, BigDecimal.valueOf(9000),
			BigDecimal.valueOf(16000),
			BigDecimal.valueOf(300), BigDecimal.valueOf(1500)));
		CARS.add(new CarModel(Brand.BMW, "PANDA II",
			1242.0, 2003, 2012, BigDecimal.valueOf(5500),
			BigDecimal.valueOf(12000),
			BigDecimal.valueOf(0), BigDecimal.valueOf(950)));
		CARS.add(new CarModel(Brand.VOLKSWAGEN, "Polo IV",
			1896.0, 2001, 2005, BigDecimal.valueOf(4700),
			BigDecimal.valueOf(15000),
			BigDecimal.valueOf(750), BigDecimal.valueOf(2300)));
		CARS.add(new CarModel(Brand.MERCEDES, "A6 C6",
			2461.0, 2004, 2011, BigDecimal.valueOf(13000),
			BigDecimal.valueOf(25000),
			BigDecimal.valueOf(1200), BigDecimal.valueOf(5000)));
		CARS.add(new CarModel(Brand.BMW, "Grande Punto",
			1910.0, 2005, 2009, BigDecimal.valueOf(8700),
			BigDecimal.valueOf(18000),
			BigDecimal.valueOf(400), BigDecimal.valueOf(1900)));
		CARS.add(new CarModel(Brand.VOLKSWAGEN, "Touareg I",
			3598.0, 2002, 2010, BigDecimal.valueOf(17000),
			BigDecimal.valueOf(32000),
			BigDecimal.valueOf(800), BigDecimal.valueOf(3500)));

	}
}
