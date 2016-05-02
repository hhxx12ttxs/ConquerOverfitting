package com.dthielke.nnet.backprop;

import java.util.ArrayList;
import java.util.List;

public class OutputNode implements Node {
    
    private static int nextId = 1;
    private final int id;
    
    private List<Link> inputs = new ArrayList<Link>();
    private DifferentiableFunction activationFunction;
    private double target;
    
    private boolean activationCached = false;
    private boolean deltaCached = false;
    
    private double activation;
    private double delta;
    
    public OutputNode(DifferentiableFunction activationFunction) {
        this.activationFunction = activationFunction;
        this.id = nextId++;
    }
    
    public void setTarget(double target) {
        this.target = target;
    }
    
    public double getTarget() {
        return target;
    }
    
    @Override
    public void clearCachedActivation() {
        activationCached = false;
    }
    
    @Override
    public void clearCachedDelta() {
        deltaCached = false;
    }
    
    @Override
    public void addLink(Link link) {
        if (link.getTail().equals(this) && !inputs.contains(link)) {
            inputs.add(link);
            link.getHead().addLink(link);
        }
    }
    
    @Override
    public double getActivation() {
        if (activationCached) {
            return activation;
        }
        
        double netInput = 0;
        for (Link link : inputs) {
            netInput += link.getHead().getActivation() * link.getWeight();
        }
        activation = activationFunction.eval(netInput);
        activationCached = true;
        return activation;
    }
    
    @Override
    public double getDelta() {
        if (deltaCached) {
            return delta;
        }
        
        double netInput = 0;
        for (Link link : inputs) {
            netInput += link.getHead().getActivation() * link.getWeight();
        }
        
        delta = activationFunction.diff(netInput) * (target - getActivation());
        deltaCached = true;
        return delta;
    }
    
    @Override
    public void train(double rate, double momentum, double decay) {
        double delta = getDelta();
        
        for (Link link : inputs) {
            link.incrementWeight(rate * delta * link.getHead().getActivation() + momentum * link.getWeightChange() - decay * link.getWeight());
        }
    }

    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return "OutputNode " + id;
    }

    @Override
    public List<Link> getLinks() {
        return inputs;
    }

}

