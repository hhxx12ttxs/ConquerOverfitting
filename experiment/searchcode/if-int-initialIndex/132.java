package ru.ifmo.ctddev.katunina.Main;

import java.util.HashMap;


public class ExpressionParser<T> {
    private String source;
    private int currentIndex = -1;

    HashMap<String, Arithmetics<T>> arithmetics;

    ConstFactory<T> constFactory;

    public ExpressionParser(String source, ConstFactory<T> constFactory, HashMap<String, Arithmetics<T>> arithmetics) {
        this.source = source;
        this.constFactory = constFactory;
        this.arithmetics = arithmetics;
    }

    public static <T> Expression3 parse(String source, ConstFactory<T> constFactory, HashMap<String, Arithmetics<T>> arithmetics) throws ParsingException {
        return new ExpressionParser<T>(source, constFactory, arithmetics).expression(false);
    }

    public Expression3 expression(boolean isExpressionInBrackets) throws ParsingException {
        Expression3 left = summand();
        while (true) {
            if (tryConsume("+"))
                left = new BinaryOperation<T>(left, summand(), arithmetics.get("+"));
            else if (tryConsume("-"))
                left = new BinaryOperation<T>(left, summand(), arithmetics.get("-"));
            else if (tryConsume(")")) {
                if (!isExpressionInBrackets)
                    throw new NoBracketException(source, currentIndex, false);
                cancelSpacesConsumption();
                currentIndex--;
                break;
            } else if (currentIndex < source.length() - 1)
                throw new IncorrectPositionException(source, currentIndex);
            else
                break;
        }

        return left;
    }

    public Expression3 summand() throws ParsingException {
        Expression3 left = multiplier(true);
        while (true) {
            if (tryConsume("*")) {
                Expression3 multiplier = multiplier(true);
                if (multiplier == null)
                    throw new IncorrectPositionException(source, currentIndex);
                left = new BinaryOperation<T>(left, multiplier, arithmetics.get("*"));
            } else if (tryConsume("/")) {
                Expression3 multiplier = multiplier(true);
                if (multiplier == null)
                    throw new IncorrectPositionException(source, currentIndex);
                left = new BinaryOperation<T>(left, multiplier, arithmetics.get("/"));
            } else if (tryConsume("mod")) {
                Expression3 multiplier = multiplier(true);
                if (multiplier == null)
                    throw new IncorrectPositionException(source, currentIndex);
                left = new BinaryOperation<T>(left, multiplier, arithmetics.get("mod"));
            } else {
                Expression3 m = multiplier(false);
                if (m != null)
                    left = new BinaryOperation<T>(left, m, arithmetics.get("*"));
                else {
                    return left;
                }
            }
        }
    }

    public Expression3 multiplier(boolean unaryOperationsAllowed) throws ParsingException {
        if (tryConsume("x") || tryConsume("y") || tryConsume("z")) {
            cancelSpacesConsumption();
            return new Variable(Character.toString(source.charAt(currentIndex)));
        } else if (unaryOperationsAllowed && tryConsume("-"))
            return new BinaryOperation<T>(new Const<T>(constFactory.getNeutral()), multiplier(true), arithmetics.get("-"));
        else if (tryConsume("(")) {
            Expression3 aux = expression(true);
            boolean closingFound = tryConsume(")");
            if (!closingFound)
                throw new NoBracketException(source, currentIndex, true);
            return aux;
        } else if (tryConsume("abs")) {
            if (tryConsume("(")) {
                Expression3 aux = expression(true);
                boolean closingFound = tryConsume(")");
                if (!closingFound)
                    throw new NoBracketException(source, currentIndex, true);
                return new BinaryOperation<T>(aux, new Const<T>(constFactory.getNeutral()), arithmetics.get("abs"));
            } else
                throw new NoBracketException(source, currentIndex, false);
        } else if (currentIndex + 1 < source.length()) {
            Const<T> c = readAllNumber(unaryOperationsAllowed);
            if (c != null)
                return c;
        }
        return null;
    }

    private boolean tryConsume(String symbol) {
        boolean result = false;
        consumeSpaces();
        if ((currentIndex + 1) < source.length() && source.substring(currentIndex + 1).startsWith(symbol)) {
            currentIndex += symbol.length();
            result = true;
            consumeSpaces();
        }
        return result;
    }

    private int lastSpacesConsumed = 0;

    //this is a horrible [-piece-of-s~--] workaround, and its existence caused only by ways, in which
    //currentIndex is used in all the other code. even the idea of this method should normally be avoided.
    private void cancelSpacesConsumption() {
        currentIndex -= lastSpacesConsumed;
        lastSpacesConsumed = 0;
    }

    //returns number of spaces consumed and sets lastSpacesConsumed into that number
    private int consumeSpaces() {
        int initialIndex = currentIndex;
        while (currentIndex + 1 < source.length() && Character.isSpaceChar(source.charAt(currentIndex + 1)))
            currentIndex++;
        return lastSpacesConsumed = currentIndex - initialIndex;
    }

    private Const<T> readAllNumber(boolean unaryOperationsAllowed) throws NumberFormatException {
        int firstIndex = currentIndex + 1;
        Const<T> result = constFactory.makeConst(source, firstIndex, unaryOperationsAllowed);
        if (result == null)
            return null;
        currentIndex += constFactory.lastConstLength;
        int spaces = consumeSpaces();
        if (spaces > 0 && constFactory.makeConst(source, currentIndex + 1, unaryOperationsAllowed) != null)
            throw new NumberFormatException(source, currentIndex);
        return result;
    }
}
