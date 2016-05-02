package task10;

import java.awt.*;
import java.awt.event.*;
import java.nio.CharBuffer;
import java.util.*;



public class Calculator {
    Frame frame;
    Panel buttonPanel;
    java.util.List<Button> buttons;
    TextField textField; 
    CalculatorBackend backend;
    
    static String[] labels = {
        "7", "8", "9", "/",
        "4", "5", "6", "*", 
        "1", "2", "3", "-", 
        "0", ".", "=", "+"
    };

    
    public Calculator() {
        frame = new Frame("Calculator");
        buttonPanel = new Panel();
        textField = new TextField();
        buttons = new ArrayList<Button>();
        for (String label: labels) {
            buttons.add(new Button(label));
        }
        backend = new CalculatorBackend();
    }

    private void drawUI() {
        frame.setLayout(new BorderLayout());

        frame.add(textField, BorderLayout.NORTH);

        buttonPanel.setLayout(new GridLayout(4, 4));
        frame.add(buttonPanel, BorderLayout.CENTER);

        for (Button b: buttons) {
            buttonPanel.add(b);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String res;
                    res = backend.performAction(e.getActionCommand().charAt(0));
                    textField.setText(res);
                }
            });
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                frame.dispose();
            }
        });

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Calculator calc = new Calculator();
        calc.drawUI();
    }


}

class CalculatorBackend {
     double lastNum;
     char lastOp = '+';
     char[] buffer;
     int bufSize;
     int maxSize;
     /*
      * Possible states:
      * q0: inputting number
      * q1: inputting operation
      * q2: pressed '='
      *
      * Transfers:
      * t# | begin | input | end
      * t1 | q0    |'.'-'9'| q0
      * t2 | q0    |'+'-'-'| q1
      * t3 | q1    |'+'-'-'| q1
      * t4 | q1    |'.'-'9'| q0
      * t5 | q0    |  '='  | q2
      * t6 | q1    |  '='  | q1 (noop)
      *
      *
      *     t1(.-9)
      *   /----\
      *   |     v
      * +---------+ t2(+,-) +---------+
      * |   {q0}  |-------->|   {q1}  |-----\
      * |inputting|         |inputting|     | t3(+, -)
      * | number  |<--------|operation|<----/
      * +---------+ t4(.-9) +---------+
      *   ^    |               ^   |
      *   |    |               |   |
      *   \----/               \---/
      *    t5(=)               t6(=)
      */
     int state;

     public CalculatorBackend(int bufSize) {
         buffer = new char[bufSize];
         this.bufSize = 0;
         maxSize = bufSize;
     }
     public CalculatorBackend() {
         this(32);
     }


     private double perform() {
         double res;
         double buf = Double.parseDouble(CharBuffer.wrap(buffer, 0, bufSize).toString());
         switch (lastOp) {
             case '+':
                 res = lastNum + buf;
                 break;
             case '-':
                 res = lastNum - buf;
                 break;
             case '*':
                 res = lastNum * buf;
                 break;
             case '/':
                 res = lastNum / buf;
                 break;
             default:
                 throw new RuntimeException("Uh oh");
         }
         return res;
     }

     private String q0(char c) {
         String output;
         
         if (Character.isDigit(c) ||
                 (c == '.' && !String.valueOf(buffer, 0, bufSize).contains(String.valueOf(c)))) {
             // t1
             if (bufSize >= maxSize - 1) {
                 return String.valueOf(buffer, 0, bufSize);
             }
             buffer[bufSize++] = c;
             output = new String(buffer, 0, bufSize);
             state = 0;
         } else if (c == '=') {
             // t5
             lastNum = perform();
             output = Double.toString(lastNum);
             state = 2;
         } else if ("+-*/".contains(String.valueOf(c))){
             // t2
             lastNum = perform();
             lastOp = c;
             output = Double.toString(lastNum);
             state = 1;
         } else {
             output = Double.toString(lastNum);
         }

         return output;
     }

     private String q1(char c) {
         String output;
         if (Character.isDigit(c) || c == '.') {
             // t4
             bufSize = 0;
             buffer[bufSize++] = c;
             state = 0;
             output = String.valueOf(c);
         } else if (c == '=') {
             output = Double.toString(lastNum);
         } else {
             //t3
             lastOp = c;
             output = Double.toString(lastNum);
         }
         return output;
     }

     private String q2(char c) {
         if (Character.isDigit(c) || c == '.'){
             // flush buffer, go to q0 and retry
             bufSize = 0;
             lastNum = 0;
             lastOp = '+';
             state = 0;
         } else if (c == '=') {
             state = 0;
         } else {
             state = 1;
         }
         return performAction(c);
     }

     public String performAction(char c) {
         int start_state = state;
         String output = "";
         switch (state) {
             case 0:
                 output = q0(c);
                 break;
             case 1:
                 output = q1(c);
                 break;
             case 2:
                 output = q2(c);
                 break;
         }

         System.out.format("q%d %c %-8s %-6.2f %c %s q%d\n", start_state, c,
                 new String(buffer, 0, bufSize), lastNum, lastOp, output, state);
         return output;
     }

 }
