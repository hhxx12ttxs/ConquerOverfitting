definition[0] = definition[0].toLowerCase();

if (MVNPenalty.LABEL.equals(definition[0])) {
p = new MVNPenalty(Double.parseDouble(definition[1]));
} else if (DirichletPenalty.LABEL.equals(definition[0])) {

