package carmarket.seller;

import carmarket.console.Printer;
import carmarket.car.RandomCarsGenerator;
import carmarket.reservation.Reservation;
import carmarket.reservation.ReservationManager;
import carmarket.buyer.BuyCarRequest;
import carmarket.car.BodyType;
import carmarket.car.Brand;
import carmarket.car.Car;
import carmarket.car.FuelType;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.Getter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class CarSellerAgent extends Agent {

	@Getter
	private List<Car> catalogue;
	@Getter
	private ReservationManager reservationManager = new ReservationManager();
	private Random random = new Random();

	@Override
	protected void setup() {
		catalogue = RandomCarsGenerator.generateCars();

		final DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		final ServiceDescription sd = new ServiceDescription();
		sd.setType("carmarket.car-carmarket.seller");
		sd.setName("JADE-carmarket.car-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (final FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new OfferRequestsServer());
		addBehaviour(new PurchaseOrdersServer());
		addBehaviour(new ReserveCarServer());
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (final FIPAException fe) {
			fe.printStackTrace();
		}
		Printer.print(
			getAID().getLocalName() + ": Seller-agent " + getAID().getName() + " terminating.");
	}

	private class OfferRequestsServer extends CyclicBehaviour {

		@Override
		public void action() {
			final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			final ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				try {
					final BuyCarRequest request = (BuyCarRequest) msg.getContentObject();

					final Optional<Car> carOffer = getBestOffer(msg.getSender().getLocalName(),
						request);
					final ACLMessage reply = msg.createReply();
					if (carOffer.isPresent()) {
						final Car car = carOffer.get();
						Printer
							.print(getAID().getLocalName() + ": Znaleziono\n" + car.toString()
								+ "\nDla zapytania\n" + request.toString());
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContentObject(car);
					} else {
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				} catch (final UnreadableException | IOException e) {
					e.printStackTrace();
					block();
				}
			} else {
				block();
			}
		}

		private Optional<Car> getBestOffer(final String buyerName, final BuyCarRequest request) {
			return catalogue.stream().filter(car -> {
				final List<Brand> brands =
					request.getBrands() == null ? new ArrayList<>() : request.getBrands();
				final List<String> models =
					request.getModels() == null ? new ArrayList<>() : request.getModels();
				final List<FuelType> fuelTypes =
					request.getFuelTypes() == null ? new ArrayList<>()
						: request.getFuelTypes();
				final List<BodyType> bodyTypes =
					request.getBodyTypes() == null ? new ArrayList<>()
						: request.getBodyTypes();
				final Double minEngineCapacity = request.getMinEngineCapacity() == null ? 0
					: request.getMinEngineCapacity();
				final Double maxEngineCapacity =
					request.getMaxEngineCapacity() == null ? Double.MAX_VALUE
						: request.getMaxEngineCapacity();
				final Integer minProductionYear =
					request.getMinProductionYear() == null ? 1800
						: request.getMinProductionYear();
				final Integer maxProductionYear =
					request.getMaxProductionYear() == null ? 2020
						: request.getMaxProductionYear();
				final BigDecimal minCost =
					request.getMinCost() == null ? BigDecimal.ZERO : request.getMinCost();
				final BigDecimal maxCost =
					request.getMaxCost() == null ? BigDecimal.valueOf(100000)
						: request.getMaxCost();
				final BigDecimal minAdditionalCost =
					request.getMinAdditionalCost() == null ? BigDecimal.ZERO
						: request.getMinAdditionalCost();
				final BigDecimal maxAdditionalCost =
					request.getMaxAdditionalCost() == null ? BigDecimal.valueOf(100000)
						: request.getMaxAdditionalCost();

				if (!brands.isEmpty() && !brands.contains(car.getBrand())) {
					return false;
				}

				if (!models.isEmpty() && models.stream()
					.noneMatch(s -> s.equalsIgnoreCase(car.getModel()))) {
					return false;
				}

				if (!fuelTypes.isEmpty() && !fuelTypes.contains(car.getFuelType())) {
					return false;
				}

				if (!bodyTypes.isEmpty() && !bodyTypes.contains(car.getBodyType())) {
					return false;
				}

				if (car.getEngineCapacity() < minEngineCapacity
					|| car.getEngineCapacity() > maxEngineCapacity) {
					return false;
				}

				if (car.getProductionYear() < minProductionYear
					|| car.getProductionYear() > maxProductionYear) {
					return false;
				}

				if (car.getCost().compareTo(minCost) < 0
					|| car.getCost().compareTo(maxCost) > 0) {
					return false;
				}

				if (car.getAdditionalCost().compareTo(minAdditionalCost) < 0
					|| car.getAdditionalCost().compareTo(maxAdditionalCost) > 0) {
					return false;
				}

				return true;

			}).filter(car -> reservationManager.get(car)
				.map(reservation1 -> reservation1.getBuyerName().equals(buyerName))
				.orElse(true)
			).min((car1, car2) -> {
				final BigDecimal cost1 = car1.getCost().add(car1.getAdditionalCost());
				final BigDecimal cost2 = car2.getCost().add(car2.getAdditionalCost());
				return cost1.compareTo(cost2);
			});
		}
	}

	private class PurchaseOrdersServer extends CyclicBehaviour {

		@Override
		public void action() {
			final MessageTemplate messageTemplate = MessageTemplate
				.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			final ACLMessage msg = myAgent.receive(messageTemplate);
			if (msg != null) {
				try {
					final Car car = (Car) msg.getContentObject();
					final ACLMessage reply = msg.createReply();
					if (reservationManager.isReserved(car) && !reservationManager
						.isReserved(msg.getSender().getLocalName(), car)) {
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("reserved");

					} else {
						if (catalogue.remove(car)) {
							reply.setPerformative(ACLMessage.CONFIRM);
							Printer.print(
								getAID().getLocalName() + ": Auto ponizej sprzedane dla kupującego "
									+ msg.getSender().getLocalName() + "\n" + car.toString());
						} else {
							reply.setPerformative(ACLMessage.FAILURE);
							reply.setContent("not-available");
						}
					}
					myAgent.send(reply);
				} catch (final UnreadableException e) {
					e.printStackTrace();
				}

			} else {
				block();
			}
		}
	}

	private class ReserveCarServer extends CyclicBehaviour {

		@Override
		public void action() {
			final MessageTemplate messageTemplate = MessageTemplate
				.MatchPerformative(ACLMessage.REQUEST);
			final ACLMessage msg = myAgent.receive(messageTemplate);
			if (msg != null) {
				try {
					final Car car = (Car) msg.getContentObject();
					final ACLMessage reply = msg.createReply();

					if (catalogue.contains(car) && !reservationManager.isReserved(car)) {
						final Reservation reservation = reservationManager
							.add(msg.getSender().getLocalName(), car,
								random.nextInt(30000) + 15000);
						addBehaviour(new RemoveReservationServer(getAgent(), reservation));
						reply.setPerformative(ACLMessage.CONFIRM);
						Printer.print(
							getAID().getLocalName() + ": Auto ponizej zarezerwowane dla kupującego "
								+ msg.getSender().getLocalName() + "\n" + car.toString());
					} else if (reservationManager.isReserved(msg.getSender().getLocalName(), car)) {
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("already-reserved");
					} else {
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-reserved");
					}

					myAgent.send(reply);
				} catch (final UnreadableException e) {
					e.printStackTrace();
				}

			} else {
				block();
			}
		}
	}

	private class RemoveReservationServer extends WakerBehaviour {

		private Reservation reservation;

		RemoveReservationServer(final Agent agent, final Reservation reservation) {
			super(agent, Date.from(reservation.getEndTime().toInstant()));
			this.reservation = reservation;
		}

		@Override
		protected void onWake() {
			reservationManager.remove(reservation);
		}
	}
}
