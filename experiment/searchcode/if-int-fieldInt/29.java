Field[] fields = myBooking.getClass().getDeclaredFields();
for (int fieldint = 0 ; fieldint<fields.length; fieldint++) {
columnNames[fieldint] = fields[fieldint].getName();
}
}

@Override
public String getColumnName(int columnIndex){

