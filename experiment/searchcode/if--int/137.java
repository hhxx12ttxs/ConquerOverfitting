/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Translator;

import Controller.CrumbData;
import GUI.CrumbPanel;
import GUI.ParameterHole;
import java.io.Serializable;

import java.util.*;

/**
 *
 * @author calvin
 */
public class TranslatorServiceImpl implements TranslatorService, Serializable
{

    private static final String INDENT = "    ";
    private static final String INCLUDES_KEY = "_includes_";
    private static final String DECLARES_KEY = "_declares_";
    private static final String SETUP_KEY = "_setup_";
    private static final String SUB_KEY = "_sub_";
    private static final List<String> RESERVED_WORDS = Arrays.asList("auto", "break", "case", "chart",
            "const", "continue", "default", "do", "double", "else", "enum", "extern",
            "float", "for", "goto", "if", "int", "long", "register", "return", "short",
            "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned",
            "void", "volatile", "while");
    private final List<String> PROBABLY_NOT_SUCH_GREAT_WORDS = Arrays.asList("setup",
            "loop", "digitalWrite", "pinMode", "Servo");
    private static final String VARIABLE_PATTERN = "^[a-zA-Z][a-zA-Z_0-9]*";
    private final Set<String> includes = new HashSet<String>();
    private final Set<String> declares = new HashSet<String>();
    private final Map<String, Integer> declaredVariables = new HashMap<String, Integer>();
    private final Set<String> setup = new HashSet<String>();
    private final Set<String> manuallyDeclaredVariables = new HashSet<String>();
    private final Set<String> manuallyDeclaredFunctions = new HashSet<String>();

    public String getCodeFromCrumbs(List<CrumbPanel> topLevelCrumbs)
    {
        CrumbPanel fatherCrumb = topLevelCrumbs.get(0);
        assert (fatherCrumb != null);
        includes.clear();
        declares.clear();
        declaredVariables.clear();
        setup.clear();
        StringBuilder builder = new StringBuilder();
        for (CrumbPanel c : topLevelCrumbs)
        {
            builder.append(buildCodeFromTree(c));
        }
        injectIncludes(builder);
        injectDeclares(builder);
        injectSetup(builder);
        return builder.toString();
    }

    private String buildCodeFromTree(CrumbPanel currentNode)
    {
        StringBuilder nodeCode = new StringBuilder();

        while (currentNode != null)
        {
            String rawCode = currentNode.getCrumbData().getCode();
            String setupCode = currentNode.getCrumbData().getSetupCode();

            for (CrumbData.Declare d : currentNode.getCrumbData().getDeclares())
            {
                if (declaredVariables.containsKey(d.getName()))
                {
                    declaredVariables.put(d.getName(), declaredVariables.get(d.getName()) + 1);
                }
                else
                {
                    declaredVariables.put(d.getName(), 1);
                }

                String formattedName = d.getDesc(); // String.format("%02d", declaredVariables.get(d.getName()).intValue());
                if (rawCode.contains(d.getName()))
                {
                    rawCode = rawCode.replaceAll(d.getName(), formattedName);
                }
                if (setupCode.contains(d.getName()))
                {
                    setupCode = setupCode.replaceAll(d.getName(), formattedName);
                }

                assert (!declares.contains(formattedName));
                String rawDeclare = d.getType() + " " + formattedName;
                rawDeclare = injectParameters(currentNode, rawDeclare, false);
                declares.add(rawDeclare);
            }

            rawCode = injectParameters(currentNode, rawCode, false);
            setupCode = injectParameters(currentNode, setupCode, false);

            nodeCode.append(rawCode);

            includes.addAll(currentNode.getCrumbData().getIncludes());
            if (setupCode != null && setupCode != "")
            {
                setup.add(setupCode);
            }

            int insertPoint = nodeCode.indexOf(SUB_KEY);
            if (insertPoint == -1)
            {
                currentNode = currentNode.getBelow() == null ? currentNode.getEnd() : currentNode.getBelow();
                continue;
            }
            nodeCode.replace(insertPoint, insertPoint + SUB_KEY.length(), "");

            if (currentNode.getChild() != null)
            {
                String subCode = buildCodeFromTree(currentNode.getChild());
                subCode = INDENT + subCode;
                subCode = subCode.replaceAll("\\n", "\n" + INDENT);
                subCode = subCode.replaceAll("\\n\\s*\\n", "\n");
                nodeCode.insert(insertPoint, subCode);
            }

            currentNode = currentNode.getBelow() == null ? currentNode.getEnd() : currentNode.getBelow();
        }

        return nodeCode.toString();
    }

    private String injectParameters(final CrumbPanel crumb, final String rawCode, boolean addParentheses)
    {
        if (rawCode == null)
        {
            return null;
        }
        String formattedCode = rawCode;
        for (ParameterHole p : crumb.getParams())
        {
            String inject = "";
            if (p.getCrumb() == null)
            {
                inject = p.getText();
            }
            else
            {
                inject = (addParentheses ? "(" : "") + injectParameters(p.getCrumb(), p.getCrumb().getCrumbData().getCode(), true) + (addParentheses ? ")" : "");
            }

            formattedCode = formattedCode.replaceAll(p.getParameterData().getName(), inject);
        }
        return formattedCode;
    }

    private void injectIncludes(StringBuilder fatherCrumb)
    {
        int insertPoint = fatherCrumb.indexOf(INCLUDES_KEY);
        assert (insertPoint != -1);
        StringBuilder includeCode = new StringBuilder();
        for (String i : includes)
        {
            includeCode.append("#include <" + i + ">\n");
        }
        fatherCrumb.replace(insertPoint, insertPoint + INCLUDES_KEY.length(), "");
        fatherCrumb.insert(insertPoint, includeCode.toString());
    }

    private void injectDeclares(StringBuilder fatherCrumb)
    {
        int insertPoint = fatherCrumb.indexOf(DECLARES_KEY);
        assert (insertPoint != -1);
        StringBuilder declareCode = new StringBuilder();
        for (String d : declares)
        {
            declareCode.append(d + ";\n");
        }
        for (String m : manuallyDeclaredVariables)
        {
            declareCode.append("int " + m + ";\n");
        }
        for (String f : manuallyDeclaredFunctions)
        {
            declareCode.append("void " + f + "();\n");
        }
        fatherCrumb.replace(insertPoint, insertPoint + DECLARES_KEY.length(), "");
        fatherCrumb.insert(insertPoint, declareCode.toString());
    }

    private void injectSetup(StringBuilder fatherCrumb)
    {
        int insertPoint = fatherCrumb.indexOf(SETUP_KEY);
        assert (insertPoint != -1);
        StringBuilder setupCode = new StringBuilder();
        for (String s : setup)
        {
            setupCode.append(INDENT + s);
        }
        fatherCrumb.replace(insertPoint, insertPoint + SETUP_KEY.length(), "");
        fatherCrumb.insert(insertPoint, setupCode.toString());
    }

    public boolean verifyNewVariableName(String newVariableName)
    {
        //Check if it's a reserved word
        if (RESERVED_WORDS.contains(newVariableName))
        {
            return false;
        }

        //Check if it's probably not a good idea to use this word
        if (PROBABLY_NOT_SUCH_GREAT_WORDS.contains(newVariableName))
        {
            return false;
        }

        //Check to see if it is already declared.
        if (manuallyDeclaredVariables.contains(newVariableName))
        {
            return false;
        }
        if (manuallyDeclaredFunctions.contains(newVariableName))
        {
            return false;
        }

        //The string has to start with a letter and can contain only letters, numbers, and underscores
        if (!newVariableName.matches(VARIABLE_PATTERN))
        {
            return false;
        }

        return true;
    }

    public void declareNewVariable(String newVariableName)
    {
        manuallyDeclaredVariables.add(newVariableName);
    }

    public void deleteDeclaredVariable(String variableToDelete)
    {
        manuallyDeclaredVariables.remove(variableToDelete);
    }

    public boolean verifyNewFunctionName(String newFunctionName)
    {
        return verifyNewVariableName(newFunctionName);
    }

    public void declareNewFunction(String newFunctionName)
    {
        manuallyDeclaredFunctions.add(newFunctionName);
    }

    public void deleteDeclaredFunction(String functionToDelete)
    {
        manuallyDeclaredFunctions.remove(functionToDelete);
    }

    public Set<String> getDeclaredVariables()
    {
        return manuallyDeclaredVariables;
    }

    public Set<String> getDeclaredFunctions()
    {
        return manuallyDeclaredFunctions;
    }
}

