LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0d);
ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.EQ, 1));

