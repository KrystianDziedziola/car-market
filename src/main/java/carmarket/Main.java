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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final int NUMBER_OF_SELLER_AGENTS = 5;

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
                carRequestWithBrand(Brand.BMW),
                carRequestWithBrand(Brand.VOLKSWAGEN),
                carRequestWithBrand(Brand.MERCEDES)
        );

        final List<BuyRequest> buyer2Requests = Arrays.asList(
                carRequestWithEngineType(FuelType.GASOLINE),
                carRequestWithEngineType(FuelType.GASOLINE),
                carRequestWithEngineType(FuelType.DIESEL)
        );

        final List<BuyRequest> buyer3Requests = Arrays.asList(
                carRequestWithBody(BodyType.HATCHBACK),
                carRequestWithBody(BodyType.SUV),
                carRequestWithBody(BodyType.SEDAN)
        );

        createBuyers(mainContainer, Arrays.asList(buyer1Requests, buyer2Requests, buyer3Requests));
    }

    private static BuyRequest carRequestWithBrand(Brand fiat) {
        return BuyRequest
                .builder()
                .brands(Collections.singletonList(fiat))
                .build();
    }

    private static BuyRequest carRequestWithEngineType(FuelType gasoline) {
        return BuyRequest
                .builder()
                .fuelTypes(Collections.singletonList(gasoline))
                .build();
    }

    private static BuyRequest carRequestWithBody(BodyType hatchback) {
        return BuyRequest
                .builder()
                .bodyTypes(Collections.singletonList(hatchback))
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
