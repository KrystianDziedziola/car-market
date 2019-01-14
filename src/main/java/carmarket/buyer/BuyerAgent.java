package carmarket.buyer;

import carmarket.console.Printer;
import carmarket.car.Car;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuyerAgent extends Agent {

    private static final int AGENT_NUMBER_ARGUMENT_INDEX = 0;
    private static final int BUY_REQUESTS_ARGUMENT_INDEX = 1;

    @Getter
    private List<BuyRequest> buyRequests = new ArrayList<>();
    @Getter
    private int agentNumber;
    private AID[] sellerAgents;
    @Getter
    private BigDecimal money = BigDecimal.valueOf(100000);

    @Override
    protected void setup() {
        Printer.print(getAID().getLocalName() + ": Has been initialized\n");
        final Object[] args = getArguments();
        if (args.length > 0) {
            this.agentNumber = (int) args[AGENT_NUMBER_ARGUMENT_INDEX];
            this.buyRequests = (List<BuyRequest>) args[BUY_REQUESTS_ARGUMENT_INDEX];

        }
        final int interval = 5000;
        addBehaviour(new TickerBehaviour(this, interval) {
            @Override
            protected void onTick() {
                if (buyRequests.stream()
                        .anyMatch(carBuyRequest -> !carBuyRequest.isProcessing())) {
                    Printer.print(getAID().getLocalName() + "Is looking for offers");
                    final DFAgentDescription template = new DFAgentDescription();
                    final ServiceDescription sd = new ServiceDescription();
                    sd.setType("carmarket.car-carmarket.seller");
                    template.addServices(sd);
                    try {
                        final DFAgentDescription[] result = DFService.search(myAgent, template);
                        Printer.print(getAID().getLocalName() + " found following sellers:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            Printer.print(sellerAgents[i].getLocalName());
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    Printer.print(getAID().getLocalName() + "is startingn\n");

                    buyRequests.stream().filter(carBuyRequest -> !carBuyRequest.isProcessing())
                            .forEach(carBuyRequest -> {
                                myAgent.addBehaviour(new BuyerAgent.RequestPerformer(carBuyRequest));
                                carBuyRequest.setProcessing(true);
                            });
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        Printer.print(getAID().getLocalName() + ": Has been terminated");
    }

    private class RequestPerformer extends Behaviour {

        private AID bestSeller;
        private BigDecimal bestPrice;
        private int repliesCount = 0;
        private MessageTemplate mt;
        private BuyerSteps step = BuyerSteps.SEARCH;
        private BuyRequest buyRequest;
        private Car bestOffer;
        private static final String CONVERSATION_ID = "carmarket.car-trade";

        RequestPerformer(final BuyRequest buyRequest) {
            this.buyRequest = buyRequest;
        }

        @Override
        public void action() {
            switch (step) {
                case SEARCH:
                    final ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID sellerAgent : sellerAgents) {
                        cfp.addReceiver(sellerAgent);
                    }
                    try {
                        cfp.setContentObject(buyRequest);
                        cfp.setConversationId(CONVERSATION_ID);
                        cfp.setReplyWith("cfp" + System.currentTimeMillis());
                        myAgent.send(cfp);
                        mt = MessageTemplate
                                .and(MessageTemplate.MatchConversationId(CONVERSATION_ID),
                                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                        step = BuyerSteps.RECEIVE_OFFERS;
                    } catch (final IOException ex) {
                        ex.printStackTrace();
                        step = BuyerSteps.END_ERROR;
                    }
                    break;
                case RECEIVE_OFFERS:
                    final ACLMessage searchReply = myAgent.receive(mt);
                    if (searchReply != null) {
                        if (searchReply.getPerformative() == ACLMessage.PROPOSE) {
                            try {
                                final Car proposal = (Car) searchReply.getContentObject();
                                final BigDecimal price = proposal.getCost()
                                        .add(proposal.getAdditionalCost());
                                if (bestSeller == null || price.compareTo(bestPrice) < 0) {
                                    bestPrice = price;
                                    bestSeller = searchReply.getSender();
                                    bestOffer = proposal;
                                }
                            } catch (final UnreadableException e) {
                                e.printStackTrace();
                            }

                        }
                        repliesCount++;
                        if (repliesCount >= sellerAgents.length) {
                            step = BuyerSteps.OFFER_REPLY;
                        }
                    } else {
                        block();
                    }
                    break;
                case OFFER_REPLY:
                    final Random random = new Random();
                    final boolean shouldReserve =
                            money.compareTo(bestPrice) < 0 || random.nextBoolean();
                    final ACLMessage order = new ACLMessage(
                            shouldReserve ? ACLMessage.REQUEST
                                    : ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    try {
                        order.setContentObject(bestOffer);
                        order.setConversationId(CONVERSATION_ID);
                        order.setReplyWith("order" + System.currentTimeMillis());
                        Printer.print(getAID().getLocalName()
                                + ": Oczekiwanie na potwierdzenie zakupu od " + bestSeller
                                .getLocalName() + "\n"
                                + bestOffer.toString());
                        myAgent.send(order);
                        mt = MessageTemplate
                                .and(MessageTemplate.MatchConversationId(CONVERSATION_ID),
                                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                        step =
                                shouldReserve ? BuyerSteps.CONFIRM_RESERVATION : BuyerSteps.CONFIRM_BUY;
                    } catch (final IOException ex) {
                        ex.printStackTrace();
                        step = BuyerSteps.END_ERROR;
                    }
                    break;
                case CONFIRM_RESERVATION:
                    final ACLMessage confirmReservation = myAgent.receive(mt);
                    if (confirmReservation != null) {
                        if (confirmReservation.getPerformative() == ACLMessage.CONFIRM) {
                            Printer.print(getAID().getLocalName()
                                    + ": Zarezerwowano auto o ponizszych parametrach u " +
                                    confirmReservation.getSender().getLocalName() + "\n" + bestOffer
                                    .toString());
                        } else if (confirmReservation.getPerformative() == ACLMessage.FAILURE) {
                            if (confirmReservation.getContent().equals("not-reserved")) {
                                Printer.print(getAID().getLocalName()
                                        + ": Rezerwacja ponizszego auta u " + confirmReservation
                                        .getSender()
                                        .getLocalName() + " nieudana\n"
                                        + bestOffer.toString());
                            } else {
                                Printer.print(getAID().getLocalName()
                                        + ": Rezerwacja poniższego auta u " + confirmReservation
                                        .getSender()
                                        .getLocalName() + " została już kiedyś dokonana\n"
                                        + bestOffer.toString());
                            }

                        }
                        step = BuyerSteps.END_SUCCESSFUL;
                    } else {
                        block();
                    }
                    break;
                case CONFIRM_BUY:
                    final ACLMessage confirmBuyReply = myAgent.receive(mt);
                    if (confirmBuyReply != null) {
                        if (confirmBuyReply.getPerformative() == ACLMessage.CONFIRM) {
                            money = money.subtract(bestPrice);
                            Printer.print(getAID().getLocalName()
                                    + ": Kupiono auto o ponizszych parametrach za "
                                    + bestPrice + " od " + confirmBuyReply
                                    .getSender().getLocalName() + "\n" + bestOffer.toString());

                            buyRequests.remove(buyRequest);
                        } else if (confirmBuyReply.getPerformative() == ACLMessage.FAILURE) {
                            if (confirmBuyReply.getContent().equals("reserved")) {
                                Printer.print(getAID().getLocalName()
                                        + ": Zakup nieudany. Auto o poniższych parametrach zostało zarezerwowane w międzyczasie\n"
                                        + bestOffer.toString());
                            } else {
                                Printer.print(getAID().getLocalName()
                                        + ": Zakup nieudany. Auto o poniższych parametrach kupiono w międzyczasie\n"
                                        + bestOffer.toString());
                            }
                        }
                        step = BuyerSteps.END_SUCCESSFUL;
                    } else {
                        block();
                    }
                    break;
                default:
                    block();
            }
        }

        @Override
        public boolean done() {
            if (step == BuyerSteps.OFFER_REPLY && bestSeller == null) {
                buyRequest.setProcessing(false);

                Printer.print(
                        getAID().getLocalName() + ": Nie ma w sprzedazy dla parametrow\n"
                                + buyRequest.toString());
                return true;
            } else if (step == BuyerSteps.END_SUCCESSFUL || step == BuyerSteps.END_ERROR) {
                buyRequest.setProcessing(false);
                return true;
            }

            return false;
        }
    }
}