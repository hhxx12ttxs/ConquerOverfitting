private final double height;
private final double invxResolution;
private final double invyResolution;
invxResolution = 1.0 / (double) xResolution;
invyResolution = 1.0 / (double) yResolution;
width = 2.0 * Math.tan(0.5 * Math.toRadians(fov));

