package utilities;

import java.util.Stack;
import com.stevesoft.pat.*;

/**
 * Convierte una expresión regular de infix a postfix.
 * Código generado con un poco de apoyo del algoritmo proporcionado por java2s.com
 * Copyright 2009 - 12 Demo Source and Support. All rights reserved
 * http://www.java2s.com/Code/Java/Collections-Data-Structure/Convertsinfixarithmeticexpressionstopostfix.htm
 * 
 * La precendencia de operadores por defecto utilizadas para la transformación son la siguientes siendo el primero el que tiene más
 * precedencia y el último el que tiene menos precendencia:
 * Operador OR (+)
 * Operador de concatenación (.)
 * Cerradura de Kleene (*)
 * 
 * Se pueden cambiar las precedencias utilizando los métodos setOrPrecence, setConcatenationPrecedence y
 * setKleenePrecedence respectivamente.
 * 
 * Para más información ver constantes en la clase OperatorManager.
 * 
 * La transformación soporta también paréntesis que pueden usarse para eliminar ambiguedades y no tener que 
 * hacer uso de las precendencias antes mencionadas.
 * 
 * No se soportan aún caracteres escapados, operador de concatenación 0 o 1 veces, operador de concatenación
 * n veces, backreferences, etc.
 * @author Ricardo Sansores
 */
public class InfixToPostfix {
    
    private static final char CLOSE_PHARENTESIS_CHAR = ')';    
    private static final char OPEN_PHARENTESIS_CHAR = '(';
    
    private Stack<Character> charStack;
    private String infixInput;
    private StringBuilder postfixOutput;
    private OperatorManager operatorManager;

    /**
     * Crea un objeto InfixToPostfix que puede transformar expresiones regulares infix a postfix.
     * Después de crear el objeto usando éste constructor es necesario llamar a setInfixInput, de
     * lo contrario al llamar a applyTransformation no se producirá un error pero se obtendrá una
     * cadena vacía como resultado de la transformación. Si desea crear el objeto y proporcionar la
     * entrada infix en un paso utilice el contrustor InfixToPostfix(String)
     */
    public InfixToPostfix() {
        init();
    }

    /**
     * Crea un objeto InfixToPostfix que puede transformar expresiones regulares infix a postfix y
     * asigna como entrada infix el parámetro proporcionado.
     * @param infixInput La cadena expresión regular en formato infix.
     */
    public InfixToPostfix(String infixInput) {
        init();
        this.infixInput = addConcatenationOperators(infixInput.replace(" ", ""));        
    }
    
    private void init(){
        this.operatorManager = new OperatorManager();
        charStack = new Stack<Character>();
        postfixOutput = new StringBuilder();
        operatorManager.resetPrecedences();
    }

    /**
     * Realiza la transformación sobre la cadena infix proporcionada durante la construcción
     * o proporcionada con el método setInfixInput y la transforma en una expresión regular 
     * en formato postfix.
     * @return La misma expresión regular proporcionada inicialmente pero en formato postfix.
     */
    public String applyTransformation() {
        for (int i = 0; i < getInfixInput().length(); i++) {
            char actualChar = getInfixInput().charAt(i);
            switch (actualChar) {
                case OperatorManager.OR_CHAR:
                case OperatorManager.CONCATENATION_CHAR:
                case OperatorManager.KLEEN_CHAR:
                    stackOperator(actualChar, operatorManager.getOperatorPrecendence(actualChar));
                    break;
                //Si encontramos un paréntesis que abre lo metemos en la pila
                case OPEN_PHARENTESIS_CHAR:
                    charStack.push(actualChar);
                    break;
                //Si encontramos un paréntesis que cierra enviamos a la función que decide maneja la pila de paréntesis
                case CLOSE_PHARENTESIS_CHAR:
                    popPharentesis();
                    break;
                //Si no es un operador simplemente lo concatenamos a la la salida postfix
                default:
                    postfixOutput = postfixOutput.append(actualChar);
                    break;
            }
        }
        while (!charStack.isEmpty()) {
            //Concatenamos todo lo que se metió en la pila porque tenía paréntesis y operadores
            postfixOutput.append(charStack.pop());
        }
        return postfixOutput.toString();
    }

    private void stackOperator(char currentChar, int precedence) {
        while (!charStack.isEmpty()) {
            char lastChar = charStack.pop();
            if (lastChar == OPEN_PHARENTESIS_CHAR) {
                charStack.push(lastChar);
                break;
            } else {
                int precendenceLastChar = operatorManager.getOperatorPrecendence(lastChar);
                if (precendenceLastChar < precedence) {
                    charStack.push(lastChar);
                    break;
                } else {
                    postfixOutput.append(lastChar);
                }
            }
        }
        charStack.push(currentChar);
    }

    private void popPharentesis() {
        while (!charStack.isEmpty()) {
            char character = charStack.pop();
            if (character == OPEN_PHARENTESIS_CHAR) {
                break;
            } else {
                postfixOutput.append(character);
            }
        }
    }

    /**
     * Devuelve la cadena de tipo infix proporcionada como entrada al objeto.
     * @return the infixInput
     */
    public String getInfixInput() {
        return infixInput;
    }

    /**
     * Establece la expresión regular en formato infix que se convertirá a formato
     * postfix. No es necesario hacer ninguna limpieza extra en el objeto InfixToPostfix,
     * al momento de setear una nueva entrada infix el objeto regresa a su estado inicial
     * y se encuentra listo para seguir llamando a su metodo applyTransformation.
     * @param infixInput the infixInput to set
     */
    public void setInfixInput(String infixInput) {
        this.infixInput = infixInput;
        charStack.clear();
        postfixOutput = new StringBuilder();
    }

    /**
     * Toma una cadena infix que no contiene caracteres de conctenación de forma explícita
     * y los ańade.
     * 
     * Ejemplos: Entrada:  abcd                Salida:  a.b.c.d
     *           Entrada:  (a+bc*)*+(ab)(ab)   Salida:  (a+b.c*)*+(a.b).(a.b)
     * @param infixInput La cadena infix que no tiene caracteres de concatenación de forma explícita.
     * @return Una cadena infix que tiene caracteres de concatenación de forma explícita
     */
    private String addConcatenationOperators(String infixInput) {
       Regex r = new Regex("([^*.+()]|\\)|\\*)([^*.+()]|\\()","${1}\\" + OperatorManager.CONCATENATION_CHAR + "${2}");       
       return r.replaceAll(r.replaceAll(infixInput));         
    }
}

