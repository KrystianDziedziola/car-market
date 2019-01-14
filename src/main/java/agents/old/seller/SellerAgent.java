package agents.old.seller;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class SellerAgent extends Agent {

    public static final String SERVICE_TYPE = "SELL";

    @Override
    protected void setup() {
        System.out.println(String.format("Seller %s started", getAID().getName()));

        register();
//        addBehaviour();
    }

    @Override
    protected void takeDown() {
        deregister();
        System.out.println(String.format("Seller %s is terminating", getAID().getName()));
    }

    private void register() {
        try {
            DFService.register(this, createAgentDescription());
        }
        catch (final FIPAException e) {
            e.printStackTrace();
        }
    }

    private DFAgentDescription createAgentDescription() {
        final DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());
        agentDescription.addServices(createServiceDescription());
        return agentDescription;
    }

    private ServiceDescription createServiceDescription() {
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(SERVICE_TYPE);
        serviceDescription.setName("Cars selling");
        return serviceDescription;
    }

    private void deregister() {
        try {
            DFService.deregister(this);
        } catch (final FIPAException e) {
            e.printStackTrace();
        }
    }
}
