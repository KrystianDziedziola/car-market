package carmarket.reservation;

import carmarket.car.Car;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class Reservation {

	private String buyer;
	private Car car;
	private ZonedDateTime expirationDate;
}
