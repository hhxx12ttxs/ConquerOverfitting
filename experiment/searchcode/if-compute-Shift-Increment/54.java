if (&quot;Home&quot;.equals(name)) home();
else if (&quot;End&quot;.equals(name)) end();
else if (&quot;IncrementValue&quot;.equals(name)) incrementValue();
slider.increment();
}
}

// Used only if snapToTicks is true.
double computeIncrement() {
final Slider slider = getControl();

