package main.atlnis.nfx.launcher;

import javax.swing.JOptionPane;

import main.atlnis.nfx.launcher.util.SetEnviroment;
int test = Integer.parseInt(System.getProperty(&quot;sun.arch.data.model&quot;) );
if(test != 64){
JOptionPane.showMessageDialog(null, &quot;Java 64bit nenalezena. Pro správnou funkènost programu prosím nainstalujte 64bitovou verzi javy.&quot;, &quot;Info&quot;, JOptionPane.WARNING_MESSAGE);

