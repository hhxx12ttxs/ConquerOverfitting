// GraphLab Project: http://graphlab.sharif.edu
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/

package graphlab.plugins.commandline.parsers;

import bsh.EvalError;
import bsh.Interpreter;
import graphlab.platform.lang.CommandAttitude;
import graphlab.platform.core.exception.ExceptionHandler;
import graphlab.plugins.commandline.Shell;
import graphlab.plugins.commandline.ShellConsole;
import graphlab.plugins.commandline.commands.ShellCommandException;
import graphlab.ui.ExtensionShellCommandProvider;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Mohammad Ali Rostatmi
 * @email ma.rostami@yahoo.com
 */
public class InwardCommandParser {
    public HashMap<String, Method> commands = new HashMap<String, Method>();
    public HashMap<String, String> abbrs = new HashMap<String, String>();

    private Interpreter interpreter;
    private Shell shell;
    public static String evaluations;


    public InwardCommandParser(Interpreter interpreter, Shell shell) {
        this.interpreter = interpreter;
        this.shell = shell;
        try {
            interpreter.set("me", this);
            interpreter.eval("help() { me.help();}");
            interpreter.eval("help(command) { me.help(command);}");
            evaluations = shell.getEvaluations();
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }

    public void help() {
        try {
            ((ShellConsole) interpreter.get("console")).print("\nTo see details of commands (arguments , full description) run command: help(\"command\")\n\n"
                    , Color.red);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
        String h = "";
        Vector<String> hh = new Vector<String>();

        for (String s : commands.keySet()) {
            hh.add(getHelpString(s));
        }

        for (Map.Entry<String, Class> e : shell.code_completion_dictionary.entrySet()) {
            String command = e.getKey();
            hh.add(getHelpString(command));
        }

        Collections.sort(hh);


        for (String s : hh)
            h += s;

        try {
            ((ShellConsole) interpreter.get("console")).println(h, Color.blue);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }

    public void help(String command) {
        String h = getHelpString(command);
        try {
            ((ShellConsole) interpreter.get("console")).println(h, Color.blue);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }

    private String getHelpString(String command) {
        String h = "";
        h += command;
        if (commands.containsKey(command)) {
            h += "(" + commands.get(command).getAnnotation(CommandAttitude.class).abbreviation() + ") :";
            h += commands.get(command).getAnnotation(CommandAttitude.class).description();
            h += "\n";
        } else if (shell.code_completion_dictionary.containsKey(command)) {
            Class claz = shell.code_completion_dictionary.get(command);
            if (claz != null) {
//                CommandAttitude an1 = (CommandAttitude) claz.getAnnotation(CommandAttitude.class);
//                if (an1 != null) {
//                    h += "(" + an1.abbreviation() + ") :";
//                    h += an1.description();
//                } if (Extension.class.isAssignableFrom(claz)) {
                ExtensionShellCommandProvider provider = ExtensionShellCommandProvider.commandsDict.get(command);
                if (provider != null) {
                    h += "(" + provider.abrv + ") :";
                    h += provider.desc;
//                    }
                }
            }
            h += "\n";
        }
        return h;
    }

    public Object evaluateCommand(String s, String name, String abbr) {
        evaluations += s;
        abbrs.put(abbr, name);
        try {
            return interpreter.eval(s);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
        return null;
    }


    /**
     * the Objects that the methods should be invoked on.
     */
    public HashMap<Method, Object> methodObjects = new HashMap<Method, Object>();

    /**
     * imports all commands stored in annotated methods of o
     *
     * @param o
     */
    public void addCommands(Object o) {
        try {
            interpreter.set("current_interpreter", interpreter);
            evaluations = "import graphlab.graph.graph.*;" + evaluations;
            evaluations = "import graphlab.ui.lang.*;" + evaluations;
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
        Class clazz = o.getClass();
        for (Method m : clazz.getMethods()) {
            CommandAttitude cm = m.getAnnotation(CommandAttitude.class);
            if (cm != null) {
                commands.put(cm.name(), m);
                abbrs.put(cm.abbreviation(), cm.name());
                methodObjects.put(m, o);

                String evaluation = cm.name() + "(";

                String temp = "";
                if (m.getParameterTypes().length != 0)
                    temp = "Object[] o = new Object[" + m.getParameterTypes().length + "];";

                int i = 0;
                for (Class c : m.getParameterTypes()) {
                    i++;
                    evaluation += c.getSimpleName() + " x" + i + "  ,";
                    temp += "o[" + (i - 1) + "] = x" + i + ";";
                }

                evaluation = (m.getParameterTypes().length == 0 ?
                        evaluation : evaluation.substring(0, evaluation.length() - 1))
                        + ")";

                if (m.getParameterTypes().length == 0)
                    evaluation += "{" + temp + "me.parseShell(\"" + cm.name() + "\"" + ",null,current_interpreter);}";
                else
                    evaluation += "{" + temp + "me.parseShell(\"" + cm.name() + "\"" + ",o,current_interpreter);}";
                evaluations += evaluation + "\n";
            }
        }
        try {
            interpreter.eval(evaluations);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }

    public Object parseShell(String command, Object[] ps, Interpreter in) throws ShellCommandException {
        Method m = commands.get(command);
        Object o = null;
        if (m != null) {
            try {
                o = m.invoke(methodObjects.get(m), ps);
                if (o != null && (o.getClass().isPrimitive() || o instanceof String)) {
//                    in.set("obj", o);
//                   in.eval("print(obj);");
                } else {
//                    if (!m.getAnnotation(CommandAttitude.class).result().equals("")) {
//                        in.eval(m.getAnnotation(CommandAttitude.class).result());
//                        in.set("obj", o);
//                        in.eval("print_out(obj);");
//                    }
                }
            } catch (IllegalAccessException e) {
                ExceptionHandler.catchException(e);
            } catch (InvocationTargetException e) {
                ExceptionHandler.catchException(e);
            } catch (NumberFormatException e) {
                ExceptionHandler.catchException(e);
            }
        } else {
            in.print("bad command!\n");
        }
        return o;
    }

    Object getValue(String value, Class valuetype) {
        if (valuetype.getName().equals("int"))
            return Integer.parseInt(value);
        else if (valuetype.getName().equals("double"))
            return Double.parseDouble(value);
        return value;

    }

}

