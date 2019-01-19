package carmarket;

import carmarket.buyer.BuyRequest;
import carmarket.buyer.BuyerAgent;
import carmarket.car.BodyType;
import carmarket.car.Brand;
import carmarket.car.FuelType;
import carmarket.seller.CarSellerAgent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final int NUMBER_OF_SELLER_AGENTS = 10;

    public static void main(final String[] args) throws Exception {
        final AgentContainer mainContainer = runApplication();
        createSellers(mainContainer);
        createBuyers(mainContainer);
    }

    private static AgentContainer runApplication() throws StaleProxyException {
        final Runtime runtime = Runtime.instance();
        runtime.setCloseVM(true);

        final AgentContainer mainContainer = runtime.createMainContainer(new ProfileImpl(null, 1200, null));
        mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]).start();
        return mainContainer;
    }

    private static void createBuyers(AgentContainer mainContainer) {
        final List<BuyRequest> buyer1Requests = Arrays.asList(
                carRequest(Brand.BMW, FuelType.GASOLINE, 10_000),
                carRequest(Brand.VOLKSWAGEN, FuelType.GAS, 20_000),
                carRequest(Brand.MERCEDES, FuelType.GAS, 15_000)
        );

        final List<BuyRequest> buyer2Requests = Arrays.asList(
                carRequest(Brand.AUDI, FuelType.GASOLINE, 55_000),
                carRequest(Brand.TOYOTA, FuelType.GAS, 50_000),
                carRequest(Brand.MERCEDES, FuelType.GAS, 15_000)
        );

        final List<BuyRequest> buyer3Requests = Arrays.asList(
                carRequest(Brand.HONDA, FuelType.DIESEL, 100_000),
                carRequest(Brand.RENAULT, FuelType.GASOLINE, 55_000),
                carRequest(Brand.MERCEDES, FuelType.DIESEL, 5_000)
        );

        createBuyers(mainContainer, Arrays.asList(buyer1Requests, buyer2Requests, buyer3Requests));
    }

    private static BuyRequest carRequest(Brand bmw, FuelType fuelType, int maxCost) {
        return BuyRequest
                .builder()
                .brands(Collections.singletonList(bmw))
                .fuelTypes(Collections.singletonList(fuelType))
                .maxCost(BigDecimal.valueOf(maxCost))
                .build();
    }

    private static void createSellers(final AgentContainer container) {
        try {
            for (int agentNumber = 1; agentNumber <= NUMBER_OF_SELLER_AGENTS; agentNumber++) {
                final Object[] arguments = {agentNumber};
                final String nickname = "Seller: " + agentNumber;

                container
                        .createNewAgent(nickname, CarSellerAgent.class.getName(), arguments)
                        .start();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void createBuyers(final AgentContainer container,
                                     final List<List<BuyRequest>> buyerRequests) {
        try {
            for (int agentNumber = 1; agentNumber <= buyerRequests.size(); agentNumber++) {
                final Object[] arguments = {agentNumber, buyerRequests.get(agentNumber-1)};
                final String nickname = "Buyer: " + agentNumber;
                container
                        .createNewAgent(nickname, BuyerAgent.class.getName(), arguments)
                        .start();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
