action = getControl().getOrientation() == Orientation.HORIZONTAL ? &quot;IncrementValue&quot; : &quot;DecrementValue&quot;;
}
} else if (e.getCode() == RIGHT || e.getCode() == KP_RIGHT) {
slider.increment();
}
}

// Used only if snapToTicks is true.
double computeIncrement() {
final Slider slider = getControl();

