public class DoubleI2CDevice extends I2CDevice<Double> {

private double factor = 1;

@Override
public Double getValue() throws Exception {
Integer value = super.getValueInt();
if (value != null) {

