package Scheme101;

import java.util.*;
import java.io.*;


public class Repl {

    public static Cell evaluate(Cell cell, Environment env) {
        if (cell.type.equals("Unknown"))
            return env.get(cell.value);
        else if (cell.type.equals("Number") || cell.type.equals("Proc") || cell.type.equals("Lambda") || cell.type.equals("Symbol"))
            return cell;
        else if (cell.type.equals("List")) {
            if(cell.list.isEmpty()){
                return cell;
            }else if (cell.list.get(0).value.equals("lambda")) {
                return new Cell("Lambda", cell.list.get(1), cell.list.get(2), new Environment(env));
            } else if (cell.list.get(0).value.equals("quote")) {
                return symbolize(cell.list.get(1));
            } else if (cell.list.get(0).value.equals("define")) {
                env.add(cell.list.get(1).value, evaluate(cell.list.get(2), env));
                return null;
            } else if (cell.list.get(0).value.equals("set!")) {
                env.set(cell.list.get(1).value, evaluate(cell.list.get(2), env));
                return null;
            } else if (cell.list.get(0).value.equals("if")) {
                if (evaluate(cell.list.get(1), env).value.equals("#t")) {
                    return evaluate(cell.list.get(2), env);
                } else {
                    return evaluate(cell.list.get(3), env);
                }
            } else if (cell.list.get(0).value.equals("begin")) {
                for (int i = 1; i < cell.list.size() - 1; i++) {
                    evaluate(cell.list.get(i), env);
                }
                return evaluate(cell.list.get(cell.list.size() - 1), env);
            } else {
                Cell nc = new Cell("List");
                for (int i = 0; i < cell.list.size(); i++) {
                    nc.list.add(i, evaluate(cell.list.get(i), env));
                }
                if (nc.list.get(0).type.equals("Proc")) {
                    try {
                        return (Cell) nc.list.get(0).method.invoke(new StandardScheme(), nc.list);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } else if (nc.list.get(0).type.equals("Lambda")) {
                    Cell Args = nc.list.get(0).args;
                    for(int i = 1;i<nc.list.size();i++){
                        nc.list.get(0).env.add(Args.list.get(i - 1).value, evaluate(nc.list.get(i), env));
                    }
                    
                    return evaluate(nc.list.get(0).body, nc.list.get(0).env);
                }
            }
        }
        return null;
    }

    public static Cell atom(String token) {
        try {
            Integer.parseInt(token);
            return new Cell("Number", token);
        } catch (Exception e) {
            return new Cell("Unknown", token);
        }
    }

    public static Cell read_from(List<String> tokens) {
        int openCounter = 0;
        int closeCounter = 0;
        int a = 0;
        if (tokens.get(0).equals("(")) {

            Cell cell = new Cell("List");
            for (int i = 1; !tokens.get(i).equals(")"); i++) {
                if (tokens.get(i).equals("(")) {
                    openCounter = 1;
                    closeCounter = 0;
                    a = i;
                    while (openCounter!=closeCounter) {
                        a++;
                        if(tokens.get(a).equals("("))
                            openCounter++;
                        else if(tokens.get(a).equals(")"))
                            closeCounter++;                    
                    }
                    cell.list.add(read_from(tokens.subList(i, a + 1)));
                    i = a;
                } else {
                    if(!tokens.get(i).equals(""))
                        cell.list.add(read_from(tokens.subList(i, i + 1)));
                }
            }
            return cell;
        }
        return atom(tokens.get(0));
    }

    public static List<String> tokenize(String s) {
        s = s.replace("(", " ( ");
        s = s.replace(")", " ) ");
        s = s.replace("\n", "");
        s = s.replace("\t", "");
        
        String[] s2 = s.split(" ");
        
        List<String> list = Arrays.asList(s2);

        list = new ArrayList(list);

        Set<String> set = new HashSet<String>();
        set.add("");

        list.removeAll(set);

        return list;
    }

    public static Cell read(String s) {
        return read_from(tokenize(s));
    }

    public static String toString(Cell cell) {
        if (cell == null) {
            return null;
        } else if (cell.type.equals("Symbol")) {
            return cell.value;
        } else if (cell.type.equals("Number")) {
            return "" + cell.value;
        } else if (cell.type.equals("Proc")) {
            return "<Procedure:" +cell.value+">";
        } else if (cell.type.equals("Lambda")) {
            return "<Lambda>";
        } else {
            String s = "(";
            for (int i = 0; i < cell.list.size(); i++) {
                s += toString(cell.list.get(i)) + " ";

            }
            if(cell.list.size() > 0)
                s = s.substring(0, s.length() - 1);
            s += ")";

            return s;
        }
    }

    public static Environment createGlobal() {
        Environment env = new Environment(null);
        Class standard = StandardScheme.class;

        env.add("#t", new Cell("Symbol", "#t"));
        env.add("#f", new Cell("Symbol", "#f"));
        env.add("null", new Cell("List"));

        try {
            Cell not = new Cell("Proc", "not");
            not.method = standard.getMethod("not", List.class);
            env.add("not", not);

            Cell equal = new Cell("Proc", "=");
            equal.method = standard.getMethod("equal", List.class);
            env.add("=", equal);
            env.add("equal?", equal);

            Cell greater = new Cell("Proc", ">");
            greater.method = standard.getMethod("greater", List.class);
            env.add(">", greater);

            Cell less = new Cell("Proc", "<");
            less.method = standard.getMethod("less", List.class);
            env.add("<", less);

            Cell greaterEqual = new Cell("Proc", ">=");
            greaterEqual.method = standard.getMethod("greaterEqual", List.class);
            env.add(">=", greaterEqual);

            Cell lessEqual = new Cell("Proc", "<=");
            lessEqual.method = standard.getMethod("lessEqual", List.class);
            env.add("<=", lessEqual);

            Cell add = new Cell("Proc", "+");
            add.method = standard.getMethod("add", List.class);
            env.add("+", add);

            Cell mul = new Cell("Proc", "*");
            mul.method = standard.getMethod("mul", List.class);
            env.add("*", mul);

            Cell sub = new Cell("Proc", "-");
            sub.method = standard.getMethod("sub", List.class);
            env.add("-", sub);

            Cell div = new Cell("Proc", "/");
            div.method = standard.getMethod("div", List.class);
            env.add("/", div);

            Cell intDiv = new Cell("Proc", "div");
            intDiv.method = standard.getMethod("intDiv", List.class);
            env.add("div", intDiv);

            Cell len = new Cell("Proc", "len");
            len.method = standard.getMethod("len", List.class);
            env.add("len", len);

            Cell cons = new Cell("Proc", "cons");
            cons.method = standard.getMethod("cons", List.class);
            env.add("cons", cons);

            Cell first = new Cell("Proc", "first");
            first.method = standard.getMethod("first", List.class);
            env.add("first", first);

            Cell rest = new Cell("Proc", "rest");
            rest.method = standard.getMethod("rest", List.class);
            env.add("rest", rest);

            Cell append = new Cell("Proc", "append");
            append.method = standard.getMethod("append", List.class);
            env.add("append", append);

            Cell list = new Cell("Proc", "list");
            list.method = standard.getMethod("list", List.class);
            env.add("list", list);

            Cell isList = new Cell("Proc", "list?");
            isList.method = standard.getMethod("isList", List.class);
            env.add("list?", isList);

            Cell isEmpty = new Cell("Proc", "empty?");
            isEmpty.method = standard.getMethod("isEmpty", List.class);
            env.add("empty?", isEmpty);
            
            Cell isNumber = new Cell("Proc", "number?");
            isNumber.method = standard.getMethod("isNumber", List.class);
            env.add("number?", isNumber);

            Cell isSymbol = new Cell("Proc", "symbol?");
            isSymbol.method = standard.getMethod("isSymbol", List.class);
            env.add("symbol?", isSymbol);

        } catch (Exception e) {
            System.out.println(e);
        }

        return env;
    }

    public static void repl() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = "";
        Environment env = createGlobal();

        while (true) {
            System.out.print("> ");
            try {
                s = in.readLine();
            } catch (Exception e) {
            }
            if (!s.equals("")) {
                s = toString(evaluate(read(s), env));
                if (s != null) {
                    System.out.println(s);
                }
            }
        }
    }

    public static void main(String[] args) {
        repl();
    }

    private static Cell symbolize(Cell cell) {
        Cell nc;
        if(cell.type.equals("List")){
            nc = new Cell("List");
            for(int i=0;i<cell.list.size();i++)
                nc.list.add(symbolize(cell.list.get(i)));
        }else {
            nc = new Cell("Symbol", cell.value);
        }

        return nc;
    }
}

