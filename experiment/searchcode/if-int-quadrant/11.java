private MineFieldQuadrantStore mineFieldQuadrantStore;


public MineField(final int size_X, final int size_Y) {
this.mineFieldQuadrantStore = new MineFieldQuadrantStore(this);
}

public void placeMine(int pos_X, int pos_Y) {
this.mineFieldQuadrantStore.getMineFieldQuadrant(new Position(pos_X, pos_Y)).setContainsMine(true);

