delayField[i].setText(String.valueOf((settings.getTriggerDelays()[i]/1000.0)));
}
//    if (settings.getHwGain() < 0)
//      gain20n.setSelected(true);
//    else if (settings.getHwGain() > 0)
add(fillerPanel,new GridBagConstraints(1,blockRow,1,1,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,insets,0,0));
blockRow++;
}
if (buttons)
{
JPanel buttonPanel = new JPanel(new GridLayout(1,0));

