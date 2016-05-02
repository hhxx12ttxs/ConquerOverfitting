/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graficar.funciones.trigonométricas;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;
import javax.swing.JOptionPane;

/**
 *
 * @author Alex
 */
public class Graficos extends javax.swing.JApplet {

    /**
     * Initializes the applet Graficos
     */
    @Override
    public void init() {
        //cambia las dimensiones del visor de Applet
        resize(350,500);
        //Le colocamos color blanco al visor de Applet
        this.getContentPane().setBackground(Color.white);

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Graficos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Graficos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Graficos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Graficos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the applet */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //Declaramos las variables
    int num1,num2,num3=0;
    String funciones;
    double punto,y = 0;
    //Método para graficar las líneas 
    public void Graficar(){
        //Condiciones que evalúan los TextField
        if (TextField_Numero1.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Ingrese un valor negativo");
            TextField_Numero1.requestFocus();
        }else{
           //Obtenemos el dato ingresado y lo convertimos de tipo int
            num1 = Integer.parseInt(TextField_Numero1.getText());
           //Evaluamos las siguientes variables 
            if (num3 < num1 || num3 == num1) {
                JOptionPane.showMessageDialog(null, "Ingrese un valor negativo menor que 0");
                TextField_Numero1.requestFocus();
            }else{
                if (TextField_Numero2.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Ingrese un valor positivo");
                    TextField_Numero2.requestFocus();
                }else{
                    //obtenemos el dato ingresado y lo convertimos de tipo int
                    num2 = Integer.parseInt(TextField_Numero2.getText());
                    //Mandamos a llamar el método getGraphics 
                    Graphics p = getGraphics();
                    p.setColor(new Color(255,255,255));//Color blanco
                             // x  y  ancho alto
                    p.fillRect(0, 100, 300,400);//Fondo para que borre las anteriores
                    p.setColor(new Color(0,0,0));//Color para las líneas
                    p.drawLine(50, 300, 300, 300); //Eje horizontal
                    p.drawLine(150, 100, 150, 350);//Eje vertical
                    /*Metedo que le da un color rojo a la línea 
                      que figura la función trigonométrica*/
                    p.setColor(new Color(255,0,0));
                    //Ciclo for que graficas las líneas que 
                    //recorren la línea horizontal y vertical
                                              // i = i + 8
                    //Ciclo for para la linea vertical
                    for (int i = 50; i < 300; i+=8) {
                        //Graficamos los puntos de los ejes
                        p.drawLine( i, 305, i, 295);
                    }
                    //Ciclo for para la linea horizontal
                    for (int j = 120; j < 350; j+=8) {
                        //Graficamos los puntos de los ejes
                        p.drawLine( 145, j, 155, j);
                    }
                    //Capturamos el valor escogido en el comboBox
                     funciones = ComboBox_Funciones.getSelectedItem().toString();
                     
                     for (double x = num1; x < num2; x+=0.01) {
                        punto = 150 + x * 30;
                        //Instrucción switch que evalúa la variable funciones
                        switch(funciones){
                            //Coseno
                            case "Cos":
                                y = 300-Math.cos(x)*30;
                                break;
                            //Seno
                             case "Sin":
                                y = 300 - Math.sin(x)*30;
                                break;
                             //Tangente
                             case "Tan":
                                y = 300 - Math.tan(x)*30;
                                break;
                        }
                        //Formamos la línea que figurara la función trigonométrica
                        p.drawLine((int)punto,(int)y,(int)punto + 1,(int)y +1);

                    }
                }
            }
        }
    }
    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TextField_Numero1 = new javax.swing.JTextField();
        TextField_Numero2 = new javax.swing.JTextField();
        Button_Graficar = new javax.swing.JButton();
        ComboBox_Funciones = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        Button_Graficar.setText("Graficar");
        Button_Graficar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Button_GraficarActionPerformed(evt);
            }
        });

        ComboBox_Funciones.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cos", "Sin", "Tan" }));

        jLabel4.setText("©Programadores HN");

        jLabel3.setText("Para mas videos tutoriales");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        jLabel3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel3MouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(TextField_Numero1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(TextField_Numero2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ComboBox_Funciones, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Button_Graficar)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(115, 115, 115)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel4)))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TextField_Numero1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TextField_Numero2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Button_Graficar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ComboBox_Funciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 160, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(10, 10, 10)
                .addComponent(jLabel4)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void Button_GraficarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Button_GraficarActionPerformed
        Graficar();
    }//GEN-LAST:event_Button_GraficarActionPerformed

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI("http://www.youtube.com/alexjpz17"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel3MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseMoved
        jLabel3.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel3.setForeground (Color.BLUE);
    }//GEN-LAST:event_jLabel3MouseMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Button_Graficar;
    private javax.swing.JComboBox ComboBox_Funciones;
    private javax.swing.JTextField TextField_Numero1;
    private javax.swing.JTextField TextField_Numero2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}

