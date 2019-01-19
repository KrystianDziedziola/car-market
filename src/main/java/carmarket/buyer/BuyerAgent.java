package carmarket.buyer;

import carmarket.console.Printer;
import carmarket.car.Car;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static carmarket.shared.Constants.CONVERSATION_ID;

@Getter
public class BuyerAgent extends Agent {

    private static final int STARTING_BALANCE = 100_000;
    private static final int INTERVAL = 5_000;

    private static final int AGENT_NUMBER_ARGUMENT_INDEX = 0;
    private static final int BUY_REQUESTS_ARGUMENT_INDEX = 1;


    public static final String SERVICE_DESCRIPTION = "cm.sellerServiceDescription";

    private List<BuyRequest> buyRequests = new LinkedList<>();
    private int agentNumber;
    private AID[] sellerAgents;
    private BigDecimal balance = BigDecimal.valueOf(STARTING_BALANCE);

    @Override
    protected void setup() {
        Printer.print(getAID().getLocalName() + ": Has been initialized wiht balance: " + balance + ".\n");
        final Object[] args = getArguments();
        this.agentNumber = (int) args[AGENT_NUMBER_ARGUMENT_INDEX];
        this.buyRequests = new LinkedList<>((List<BuyRequest>) args[BUY_REQUESTS_ARGUMENT_INDEX]);

        addBehaviour(createBuyerBehaviour(INTERVAL));
    }

    private TickerBehaviour createBuyerBehaviour(int interval) {
        return new TickerBehaviour(this, interval) {
            @Override
            protected void onTick() {
                if (isAnyRequestUnprocessed()) {
                    Printer.print(getAID().getLocalName() + " is looking for offers.");

                    try {
                        final DFAgentDescription[] result = DFService.search(myAgent, createTemplate());

                        addSellers(result);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                    startBuying();
                }
            }

            private void startBuying() {
                buyRequests
                        .stream()
                        .filter(isUnprocessed())
                        .forEach(request -> {
                            myAgent.addBehaviour(new RequestBehaviour(request));
                            request.setProcessing(true);
                        });
            }
        };
    }

    private DFAgentDescription createTemplate() {
        final DFAgentDescription template = new DFAgentDescription();
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(SERVICE_DESCRIPTION);
        template.addServices(serviceDescription);
        return template;
    }

    private void addSellers(DFAgentDescription[] result) {
        sellerAgents = new AID[result.length];
        IntStream.range(0, result.length)
                .forEach(resultNumber -> sellerAgents[resultNumber] = result[resultNumber].getName());
    }

    private Predicate<BuyRequest> isUnprocessed() {
        return request -> !request.isProcessing();
    }

    private boolean isAnyRequestUnprocessed() {
        return buyRequests
                .stream()
                .anyMatch(isUnprocessed());
    }

    @Override
    protected void takeDown() {
        Printer.print(getAID().getLocalName() + ": has been terminated");
    }

    private class RequestBehaviour extends Behaviour {

        private AID bestSellerAid;
        private BigDecimal bestPrice;
        private int repliesCount = 0;
        private MessageTemplate messageTemplate;
        private BuyingStages currentBuyingStep = BuyingStages.SEARCHING;
        private BuyRequest buyRequest;
        private Car bestOffer;

        RequestBehaviour(final BuyRequest buyRequest) {
            this.buyRequest = buyRequest;
        }

        @Override
        public void action() {
            switch (currentBuyingStep) {
                case SEARCHING:
                    search();
                    break;
                case GETTING_OFFERS:
                    getOffer();
                    break;
                case REPLYING_TO_OFFERS:
                    replyToOffer();
                    break;
                case CONFIRMING_RESERVATION:
                    confirmReservation();
                    break;
                case CONFIRMING_PURCHASE:
                    confirmPurchase();
                    break;
                default:
                    block();
            }
        }

        private void search() {
            try {
                final ACLMessage aclMessage = createSearchMessage();
                myAgent.send(aclMessage);
                messageTemplate = MessageTemplate
                        .and(
                                MessageTemplate.MatchConversationId(CONVERSATION_ID),
                                MessageTemplate.MatchInReplyTo(aclMessage.getReplyWith()));
                currentBuyingStep = BuyingStages.GETTING_OFFERS;
            } catch (final Exception e) {
                e.printStackTrace();
                currentBuyingStep = BuyingStages.ERROR;
            }
        }

        private ACLMessage createSearchMessage() throws Exception {
            final ACLMessage aclMessage = new ACLMessage(ACLMessage.CFP);
            for (final AID sellerAgent : sellerAgents) {
                aclMessage.addReceiver(sellerAgent);
            }

            aclMessage.setContentObject(buyRequest);
            aclMessage.setConversationId(CONVERSATION_ID);
            aclMessage.setReplyWith("aclMessage" + System.currentTimeMillis());

            return aclMessage;
        }

        private void getOffer() {
            final ACLMessage searchReply = myAgent.receive(messageTemplate);
            if (searchReply != null) {
                if (matches(searchReply, ACLMessage.PROPOSE)) {
                    try {
                        final Car carProposal = (Car) searchReply.getContentObject();
                        final BigDecimal price = createPrice(carProposal);
                        if (bestSellerAid == null || price.compareTo(bestPrice) < 0) {
                            bestPrice = price;
                            bestSellerAid = searchReply.getSender();
                            bestOffer = carProposal;
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                }
                repliesCount++;
                if (hasMoreRepliesThanSellers()) {
                    currentBuyingStep = BuyingStages.REPLYING_TO_OFFERS;
                }
            } else {
                block();
            }
        }

        private void replyToOffer() {
            final ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            order.addReceiver(bestSellerAid);
            try {
                final String buyerName = getAID().getLocalName();
                final String bestSellerName = bestSellerAid.getLocalName();

                order.setContentObject(bestOffer);
                order.setConversationId(CONVERSATION_ID);
                order.setReplyWith("order" + System.currentTimeMillis());
                Printer.print(MessageFormat.format("{0} is waiting for purchase acceptance from {1}.\n" +
                        "Details: {2}\n\n", buyerName, bestSellerName, bestOffer));
                myAgent.send(order);
                messageTemplate = MessageTemplate
                        .and(MessageTemplate.MatchConversationId(CONVERSATION_ID),
                                MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                currentBuyingStep = BuyingStages.CONFIRMING_PURCHASE;
            } catch (final Exception ex) {
                ex.printStackTrace();
                currentBuyingStep = BuyingStages.ERROR;
            }
        }

        private void confirmPurchase() {
            final String buyerName = getAID().getLocalName();

            final ACLMessage purchaseConfirmedMessage = myAgent.receive(messageTemplate);
            if (purchaseConfirmedMessage != null) {
                if (matches(purchaseConfirmedMessage, ACLMessage.CONFIRM)) {
                    changeBalance();

                    final String sellerName = purchaseConfirmedMessage
                            .getSender()
                            .getLocalName();

                    Printer.print(MessageFormat.format("Buyer: {0} bought a car from {1} for {2}. " +
                                    "\nDetails: {3}\n",
                            buyerName,
                            sellerName,
                            bestPrice,
                            bestOffer));

                    Printer.print(MessageFormat.format("{0}: balance is now: {1}\n\n", buyerName, balance));

                    buyRequests.remove(buyRequest);
                } else if (matches(purchaseConfirmedMessage, ACLMessage.FAILURE)) {
                    Printer.print(buyerName + ": Purchase error.");
                }
                currentBuyingStep = BuyingStages.FINALIZING;
            } else {
                block();
            }
        }

        private void changeBalance() {
            balance = balance.subtract(bestPrice);
        }

        private boolean matches(ACLMessage message, int status) {
            return message.getPerformative() == status;
        }

        private void confirmReservation() {
            final ACLMessage confirmReservation = myAgent.receive(messageTemplate);
            if (confirmReservation != null) {
                final String buyerName = getAID().getLocalName();
                final String sellerName = confirmReservation.getSender().getLocalName();

                if (matches(confirmReservation, ACLMessage.CONFIRM)) {
                    Printer.print(MessageFormat.format("{0} has reserved a car from {1}. " +
                                    "\nDetails: {2}\n\n",
                            buyerName,
                            sellerName,
                            bestOffer
                    ));

                } else if (matches(confirmReservation, ACLMessage.FAILURE)) {
                    Printer.print(buyerName + ": Reservation failed");
                }
                currentBuyingStep = BuyingStages.FINALIZING;
            } else {
                block();
            }
        }

        private boolean hasMoreRepliesThanSellers() {
            return repliesCount >= sellerAgents.length;
        }

        private BigDecimal createPrice(Car proposal) {
            return proposal.getCost()
                    .add(proposal.getAdditionalCost());
        }

        @Override
        public boolean done() {
            if (isThereNoOffer()) {
                buyRequest.setProcessing(false);

                Printer.print(
                        getAID().getLocalName() + ": There is no car for given request:\n" + buyRequest.toString());
                return true;
            } else if (isEndedWithError()) {
                buyRequest.setProcessing(false);
                return true;
            }

            return false;
        }

        private boolean isEndedWithError() {
            return currentBuyingStep == BuyingStages.FINALIZING || currentBuyingStep == BuyingStages.ERROR;
        }

        private boolean isThereNoOffer() {
            return currentBuyingStep == BuyingStages.REPLYING_TO_OFFERS && bestSellerAid == null;
        }
    }
}