package uk.ac.rhul.cs.dice.golem.conbine.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscreteValueBucket extends ValueBucket {
    private final List<Object> values;

    public DiscreteValueBucket(String id, double... values) {
        super(id);
        this.values = new ArrayList<>(values.length);
        for (double value : values) {
            this.values.add(value);
        }
    }

    public DiscreteValueBucket(String id, int... values) {
        super(id);
        this.values = new ArrayList<>(values.length);
        for (int value : values) {
            this.values.add(value);
        }
    }

    public DiscreteValueBucket(String id, Ratio... values) {
        super(id);
        this.values = new ArrayList<>(values.length);
        for (Ratio value : values) {
            this.values.add(value);
        }
    }

    /**
     * Returns one of the values specified, chosen pseudo-randomly.
     */
    @Override
    public Object pickValue() {
        return values.get(super.nextInt(values.size()));
    }

    public Object getMinValue() {
        if (values.size() == 0) {
            return null;
        }

        Object smallest = values.get(0);

        for (Object value : values) {
            if (value instanceof Double) {
                if ( (Double) value < (Double) smallest ) {
                    smallest = value;
                    continue;
                }
            }

            if (value instanceof Integer) {
                if ( (Integer) value < (Integer) smallest ) {
                    smallest = value;
                }
            }

            if (value instanceof Ratio) {
                if ( ((Ratio) value).value() < ((Ratio) smallest).value() ) {
                    smallest = value;
                }
            }
        }

        return smallest;
    }

    public Object getMaxValue() {
        if (values.size() == 0) {
            return null;
        }

        Object largest = values.get(0);

        for (Object value : values) {
            if (value instanceof Double) {
                if ( (Double) value > (Double) largest ) {
                    largest = value;
                    continue;
                }
            }

            if (value instanceof Integer) {
                if ( (Integer) value > (Integer) largest ) {
                    largest = value;
                }
            }

            if (value instanceof Ratio) {
                if ( ((Ratio) value).value() > ((Ratio) largest).value() ) {
                    largest = value;
                }
            }
        }

        return largest;
    }

}

