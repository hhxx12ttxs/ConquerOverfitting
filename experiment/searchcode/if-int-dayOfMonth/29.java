private int year = 2000;
private int dayOfMonth = 1;
private int month = 1;

private DatePickerDialog datePickerDialog;
return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
}

@Override
public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

