package agents.old.buyer;

import jade.core.Agent;

public class BuyerAgent extends Agent {

    private static final long TICK_PERIOD = 1_000L;

    @Override
    protected void setup() {
        System.out.println("Hello, I'm !" + getAID().getName());
        addBehaviour(new BuyBehaviour(this, TICK_PERIOD));
    }

    @Override
    protected void takeDown() {
        System.out.println(String.format("Agent %s is terminating", getAID().getName()));
    }
}
