this.context = context;

}

@Override
public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
if(dayOfMonth <= 9 &amp;&amp; monthOfYear > 8 ){
PersonalDetailsActivity.expiryD.setText(&quot;0&quot; + dayOfMonth + &quot;/&quot; + (monthOfYear + 1) + &quot;/&quot; + year);

