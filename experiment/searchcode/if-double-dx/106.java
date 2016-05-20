package ru.etu.astamir.model.commands;

import com.google.common.base.Preconditions;
import ru.etu.astamir.geom.common.java.Direction;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.common.Pair;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 06.05.12
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class MoveCommand implements Command {
    private TopologyElement invoker;
    private Movable source;
    private double dx = 0.0;
    private double dy = 0.0;
    
    private MoveCommand(TopologyElement invoker, TopologyElement element, double dx, double dy) {
        Preconditions.checkArgument(element instanceof Movable);
        this.source = (Movable) element;
        this.invoker = invoker;
        this.dx = dx;
        this.dy = dy;
    }

    private MoveCommand(TopologyElement invoker, TopologyElement element, Pair<Double, Double> dPair) {
        this(invoker, element, dPair.left, dPair.right);
    }

    public MoveCommand(TopologyElement invoker, TopologyElement element, Direction direction, double length) {
        this(invoker, element, toDxDy(direction, length));
    }

    private MoveCommand(TopologyElement invoker, Movable element, Pair<Double, Double> dPair) {
        Preconditions.checkArgument(element instanceof TopologyElement);
        this.source = element;
        this.invoker = invoker;
        this.dx = dPair.left;
        this.dy = dPair.right;
    }

    public MoveCommand(TopologyElement invoker, Movable element, Direction direction, double length) {
        this(invoker, element, toDxDy(direction, length));
    }   
    
    public static Pair<Double, Double> toDxDy(Direction direction, double length) {
        double signedD = length * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            return Pair.of(signedD, 0.0);
        } else {
            return Pair.of(0.0, signedD);
        }
    }
    
    public TopologyElement getSource() {
        return (TopologyElement) source; // it's alright since we make sure we are TopologyElement in constructor
    }

    public TopologyElement getInvoker() {
        return invoker;
    }

    @Override
    public void execute() {
        source.move(dx, dy);
    }

    @Override
    public void unexecute() {
        source.move(-dx, -dy);
    }
}

