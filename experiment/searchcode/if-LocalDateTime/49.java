public SensorValue build() {

if (localDateTime == null){ throw new NullPointerException(&quot;localDateTime is null&quot;);}
if (deviceType == null){ throw new NullPointerException(&quot;localDateTime is null&quot;);}

return new SensorValue(this);
}
}
}

