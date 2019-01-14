package carmarket.reservation;

import carmarket.car.Car;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationManager {

	private List<Reservation> reservations = new ArrayList<>();

	public synchronized boolean isReserved(final String buyerName, final Car car) {
		return reservations.stream().anyMatch(
			reservation -> reservation.getCar().equals(car) && reservation.getBuyerName()
				.equals(buyerName));
	}

	public synchronized Reservation add(final String buyerName, final Car car,
                                        final long timeMillis) {
		Reservation reservation = null;
		if (!isReserved(buyerName, car)) {
			reservation = new Reservation(buyerName, car, ZonedDateTime.now().plus(timeMillis,
				ChronoUnit.MILLIS));
			reservations.add(reservation);
		}
		return reservation;
	}

	public synchronized void remove(final Reservation reservation) {

		reservations.remove(reservation);
	}

	public synchronized boolean isReserved(final Car car) {
		return get(car).isPresent();
	}

	public synchronized Optional<Reservation> get(final Car car) {
		return reservations.stream().filter(reservation -> reservation.getCar().equals(car))
			.findAny();
	}

}
