private Vector3f yAxis = new Vector3f(0, 1, 0);

private float speed;
private int forwardKey;
private int backKey;
private int leftKey;
private int rightKey;
private int upKey;
private int downKey;

public FlyMove() {
this(10, Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_SPACE, Input.KEY_LCONTROL);

