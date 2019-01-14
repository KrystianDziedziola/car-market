package agents.old.buyer;

import agents.old.seller.SellerAgent;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Arrays;
import java.util.List;

public class BuyBehaviour extends TickerBehaviour {

    BuyBehaviour(final Agent agent, final long period) {
        super(agent, period);
    }

    @Override
    protected void onTick() {
        System.out.println(String.format("Tick on %s agent", myAgent.getAID().getName()));
        final List<DFAgentDescription> sellerAgents = searchSellerAgents();

        myAgent.doDelete();
    }

    private List<DFAgentDescription> searchSellerAgents() {
        try {
            final DFAgentDescription[] agents = DFService.search(myAgent, sellerAgentDescription());
            return Arrays.asList(agents);
        } catch (final FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    private DFAgentDescription sellerAgentDescription() {
        final DFAgentDescription agentDescription = new DFAgentDescription();
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(SellerAgent.SERVICE_TYPE);
        agentDescription.addServices(serviceDescription);
        return agentDescription;
    }
}
