package carmarket.seller;

import carmarket.console.Printer;
import carmarket.car.CarsGenerator;
import carmarket.reservation.Reservation;
import carmarket.reservation.ReservationHandler;
import carmarket.buyer.BuyRequest;
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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import lombok.Getter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;

public class CarSellerAgent extends Agent {

    private static final String NOT_AVAILABLE = "not-available";
    private static final String RESERVED = "reserved";
    private static final String NOT_RESERVED = "not-reserved";
    private static final String ALREADY_RESERVED = "already-reserved";
    public static final String SELLER_DESCRIPTION_TYPE = "cm.sellerServiceDescription";
    public static final String SELLER_DESCRIPTION_NAME = "JADE-cm.car-trading";

    @Getter
    private List<Car> carsInOffer;
    @Getter
    private ReservationHandler reservationHandler = new ReservationHandler();

    @Override
    protected void setup() {
        try {
            carsInOffer = CarsGenerator.generate();
            DFService.register(this, createAgentDescription());

            addBehaviour(new ChooseOfferBehaviour());
            addBehaviour(new PurchaseBehaviour());
            addBehaviour(new ReserveBehaviour());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private DFAgentDescription createAgentDescription() {
        final DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(SELLER_DESCRIPTION_TYPE);
        serviceDescription.setName(SELLER_DESCRIPTION_NAME);

        agentDescription.addServices(serviceDescription);
        return agentDescription;
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            Printer.print(MessageFormat.format("{0}: is terminating.", getAID().getLocalName()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private class ChooseOfferBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            final MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            final ACLMessage message = myAgent.receive(messageTemplate);
            if (message != null) {
                handleMessage(message);
            } else {
                block();
            }
        }

        private void handleMessage(final ACLMessage message) {
            try {
                final BuyRequest request = (BuyRequest) message.getContentObject();
                final String buyerName = message.getSender().getLocalName();
                final String sellerName = getAID().getLocalName();

                final Optional<Car> carOffer = getBestOffer(buyerName, request);
                final ACLMessage reply = message.createReply();

                if (carOffer.isPresent()) {
                    final Car car = carOffer.get();
                    Printer.print(MessageFormat.format("\n{0} found car:\n {1}\nfrom: {2}\nfor request: \n {3}",
                            buyerName, car.toString(), sellerName, request.toString()));
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContentObject(car);
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent(NOT_AVAILABLE);
                }
                myAgent.send(reply);
            } catch (final Exception e) {
                e.printStackTrace();
                block();
            }
        }

        private Optional<Car> getBestOffer(final String buyerName, final BuyRequest request) {
            return carsInOffer
                    .stream()
                    .filter(doesNotMeetRequirements(request))
                    .filter(isReservedByBuyer(buyerName)
                    ).min(this::compareCost);
        }

        private Predicate<Car> doesNotMeetRequirements(BuyRequest request) {
            return car -> {
                final List<Brand> brands = request.getBrands();
                final List<String> models = request.getModels();
                final List<FuelType> fuelTypes = request.getFuelTypes();
                final List<BodyType> bodyTypes = request.getBodyTypes();
                final Double minEngineCapacity = request.getMinEngineCapacity();
                final Double maxEngineCapacity = request.getMaxEngineCapacity();
                final Integer minProductionYear = request.getMinProductionYear();
                final Integer maxProductionYear = request.getMaxProductionYear();
                final BigDecimal minCost = request.getMinCost();
                final BigDecimal maxCost = request.getMaxCost();
                final BigDecimal minAdditionalCost = request.getMinAdditionalCost();
                final BigDecimal maxAdditionalCost = request.getMaxAdditionalCost();

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
            };
        }

        private Predicate<Car> isReservedByBuyer(String buyerName) {
            return car -> reservationHandler.get(car)
                    .map(reservation1 -> reservation1.getBuyer().equals(buyerName))
                    .orElse(true);
        }

        private int compareCost(final Car car1, final Car car2) {
            final BigDecimal cost1 = car1.getCost().add(car1.getAdditionalCost());
            final BigDecimal cost2 = car2.getCost().add(car2.getAdditionalCost());
            return cost1.compareTo(cost2);
        }
    }

    private class PurchaseBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            final MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            final ACLMessage message = myAgent.receive(messageTemplate);
            if (message != null) {
                handleMessage(message);
            } else {
                block();
            }
        }

        private void handleMessage(ACLMessage message) {
            try {
                final Car car = (Car) message.getContentObject();
                final ACLMessage reply = message.createReply();

                final String buyerName = message.getSender().getLocalName();
                final String sellerName = getAID().getLocalName();

                if (isReserved(car, buyerName)) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent(RESERVED);
                } else {
                    if (carsInOffer.remove(car)) {
                        reply.setPerformative(ACLMessage.CONFIRM);
                        Printer.print(MessageFormat.format("{0} sold a car to {1}.\n Details: {2}",
                                sellerName, buyerName, car));
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent(NOT_AVAILABLE);
                    }
                }
                myAgent.send(reply);
            } catch (final UnreadableException e) {
                e.printStackTrace();
            }
        }

        private boolean isReserved(Car car, String buyerName) {
            return reservationHandler.isReserved(car) && !reservationHandler.isAnyReserved(buyerName, car);
        }
    }

    private class ReserveBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            final MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            final ACLMessage msg = myAgent.receive(messageTemplate);
            if (msg != null) {
                handleMessage(msg);
            } else {
                block();
            }
        }

        private void handleMessage(final ACLMessage message) {
            try {
                final Car car = (Car) message.getContentObject();
                final ACLMessage reply = message.createReply();

                final String buyerName = message.getSender().getLocalName();
                final String sellerName = getAID().getLocalName();

                if (carsInOffer.contains(car) && !reservationHandler.isReserved(car)) {
                    final Reservation reservation = reservationHandler.add(buyerName, car, generateReservationTime());
                    addBehaviour(new RemoveReservationBehaviour(getAgent(), reservation));

                    reply.setPerformative(ACLMessage.CONFIRM);
                    Printer.print(MessageFormat.format("{0} reserved a card for buyer {1}.\n Details: {2}",
                            sellerName, buyerName, car));
                } else if (reservationHandler.isAnyReserved(buyerName, car)) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent(ALREADY_RESERVED);
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent(NOT_RESERVED);
                }

                myAgent.send(reply);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        private int generateReservationTime() {
            return new Random().nextInt(30000) + 15000;
        }
    }

    private class RemoveReservationBehaviour extends WakerBehaviour {

        private Reservation reservation;

        RemoveReservationBehaviour(final Agent agent, final Reservation reservation) {
            super(agent, Date.from(reservation.getExpirationDate().toInstant()));
            this.reservation = reservation;
        }

        @Override
        protected void onWake() {
            reservationHandler.remove(reservation);
        }
    }
}
