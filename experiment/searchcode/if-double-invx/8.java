private final double height;
private final double invxResolution;
private final double invyResolution;
this.basis = new OrthonormalBasis(lookat.scale(-1), up);

invxResolution = 1.0 / (double) xResolution;

