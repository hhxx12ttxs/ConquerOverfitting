int factor = MathUtils.generateRandomNumber(1, 20);
String text = &quot;+&quot; + String.valueOf(factor);
int newResult = result + factor;

Move move = new Move(operation, factor, text, newResult);
moves.add(move);

