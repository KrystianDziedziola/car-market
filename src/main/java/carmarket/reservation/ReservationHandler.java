package carmarket.reservation;

import carmarket.car.Car;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ReservationHandler {

    private List<Reservation> reservations = new ArrayList<>();

    public synchronized boolean isAnyReserved(final String buyer, final Car car) {
        return reservations
                .stream()
                .anyMatch(isReserved(buyer, car));
    }

    private Predicate<Reservation> isReserved(String buyer, Car car) {
        return reservation -> reservation.getCar().equals(car)
                && reservation.getBuyer().equals(buyer);
    }

    public synchronized Reservation add(final String buyer, final Car car, final long time) {
        Reservation reservation = null;
        if (!isAnyReserved(buyer, car)) {
            reservation = new Reservation(buyer, car, ZonedDateTime.now().plus(time, ChronoUnit.MILLIS));
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
        return reservations
                .stream()
                .filter(equalsCar(car))
                .findAny();
    }

    private Predicate<Reservation> equalsCar(Car car) {
        return reservation -> reservation.getCar().equals(car);
    }

}
