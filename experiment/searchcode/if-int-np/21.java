case DialogInterface.BUTTON_POSITIVE:
int np_6_value = np_6.getValue();

if (np_6_value == 1)
np_6_value = 1;
else if (np_6_value == 2)
newInt = newInt * 100;

if ((int)newInt > 5)
np_5.setValue(0);
else
np_5.setValue(5);
}

public double convertMultiIntToSingle(){

