super(&quot;DVD&quot;);
}

public double getCosto(int days) {
if(days<=2) {
return days*1.50;
} else {
return 3 + (days-2)*2;
}
}
}

