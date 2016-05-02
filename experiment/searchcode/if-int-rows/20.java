public class Block {
	private int id;

	private int rotationState;

	private Space[] occupied;

	private Space refSpace;

	private static final int UNIQUE_IDS = 7;

	// do not change!
	private static final int SPACES_PER_BLOCK = 4;

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Returns the occupied.
	 */
	public Space[] getOccupied() {
		return occupied;
	}

	/**
	 * @param occupied
	 *            The occupied to set.
	 */
	public void setOccupied(Space[] occupied) {
		this.occupied = occupied;
	}

	public Block(Block b) {
		this.id = b.id;
		this.occupied = new Space[b.occupied.length];
		for (int i = 0; i < b.occupied.length; ++i) {
			this.occupied[i] = new Space(b.occupied[i]);
		}
		this.refSpace = new Space(b.refSpace);
		this.rotationState = b.rotationState;
	}

	public Block(int id) {
		super();
		this.id = id;
		int centercol = (int) TetrisPanel.getCOLUMNS() / 2;
		if (id == 0) {
			this.rotationState = 0;
			this.refSpace = new Space(1, centercol);
			this.buildFromRef();
		} else if (id == 1) {
			this.rotationState = 0;
			this.refSpace = new Space(1, centercol);
			this.buildFromRef();
		} else if (id == 2) {
			this.rotationState = 0;
			this.refSpace = new Space(1, centercol);
			this.buildFromRef();
		} else if (id == 3) {
			this.rotationState = 0;
			this.refSpace = new Space(1, centercol + 1);
			this.buildFromRef();
		} else if (id == 4) {
			this.rotationState = 1;
			this.refSpace = new Space(3, centercol - 1);
			this.buildFromRef();
		} else if (id == 5) {
			this.rotationState = 1;
			this.refSpace = new Space(2, centercol);
			this.buildFromRef();
		} else if (id == 6) {
			this.rotationState = 1;
			this.refSpace = new Space(3, centercol - 1);
			this.buildFromRef();
		}
	}

	public boolean insideBounds(int[] rows, int[] cols) {
		for (int i = 0; i < rows.length; ++i) {
			if (rows[i] < 1 || rows[i] > TetrisPanel.getROWS()) {
				return false; // do not fail on Block construction!
			}
		}
		for (int i = 0; i < cols.length; ++i) {
			if (cols[i] < 1 || cols[i] > TetrisPanel.getCOLUMNS()) {
				return false; // do not fail on Block construction!
			}
		}
		return true;
	}

	// TODO check wall hit
	// use tmp then set?
	/**
	 * @return Success or failure
	 */
	public boolean buildFromRef() {
		if (id == 0) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				// check
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2, refrow + 3 };
				 * int[] cols = { refcol }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				// set
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 2, refcol);
				this.occupied[3] = new Space(refrow + 3, refcol);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() + 2;
				int refcol = refSpace.getColnum() - 1;
				/*
				 * int[] rows = { refrow }; int[] cols = { refcol, refcol + 1,
				 * refcol + 2, refcol + 3 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow, refcol + 2);
				this.occupied[3] = new Space(refrow, refcol + 3);
			}
		} else if (id == 1) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow + 1 }; int[] cols = { refcol,
				 * refcol + 1 }; if (!insideBounds(rows, cols)) { return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow + 1, refcol);
				this.occupied[3] = new Space(refrow + 1, refcol + 1);
			}
		} else if (id == 2) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2 }; int[] cols = {
				 * refcol, refcol + 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 2, refcol);
				this.occupied[3] = new Space(refrow + 2, refcol + 1);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() + 1;
				int refcol = refSpace.getColnum() + 2;
				/*
				 * int[] rows = { refrow }; int[] cols = { refcol, refcol - 1,
				 * refcol - 2 }; if (!insideBounds(rows, cols)) { return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol - 1);
				this.occupied[2] = new Space(refrow, refcol - 2);
				this.occupied[3] = new Space(refrow + 1, refcol - 2);
			} else if (rotationState == 2) {
				int refrow = refSpace.getRownum() + 2;
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow - 1, refrow - 2 }; int[] cols = {
				 * refcol, refcol - 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow - 1, refcol);
				this.occupied[2] = new Space(refrow - 2, refcol);
				this.occupied[3] = new Space(refrow - 2, refcol - 1);
			} else if (rotationState == 3) {
				int refrow = refSpace.getRownum() + 2;
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow - 1 }; int[] cols = { refcol,
				 * refcol + 1, refcol + 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow, refcol + 2);
				this.occupied[3] = new Space(refrow - 1, refcol + 2);
			}
		} else if (id == 3) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2 }; int[] cols = {
				 * refcol, refcol - 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 2, refcol);
				this.occupied[3] = new Space(refrow + 2, refcol - 1);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() + 2;
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow - 1 }; int[] cols = { refcol,
				 * refcol - 1, refcol - 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol - 1);
				this.occupied[2] = new Space(refrow, refcol - 2);
				this.occupied[3] = new Space(refrow - 1, refcol - 2);
			} else if (rotationState == 2) {
				int refrow = refSpace.getRownum() + 2;
				int refcol = refSpace.getColnum() - 1;
				/*
				 * int[] rows = { refrow, refrow - 1, refrow - 2 }; int[] cols = {
				 * refcol, refcol + 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow - 1, refcol);
				this.occupied[2] = new Space(refrow - 2, refcol);
				this.occupied[3] = new Space(refrow - 2, refcol + 1);
			} else if (rotationState == 3) {
				int refrow = refSpace.getRownum() + 1;
				int refcol = refSpace.getColnum() - 1;
				/*
				 * int[] rows = { refrow, refrow + 1 }; int[] cols = { refcol,
				 * refcol + 1, refcol + 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow, refcol + 2);
				this.occupied[3] = new Space(refrow + 1, refcol + 2);
			}
		} else if (id == 4) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow - 1 }; int[] cols = { refcol,
				 * refcol + 1, refcol + 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow - 1, refcol + 1);
				this.occupied[3] = new Space(refrow - 1, refcol + 2);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() - 2;
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2 }; int[] cols = {
				 * refcol, refcol + 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 1, refcol + 1);
				this.occupied[3] = new Space(refrow + 2, refcol + 1);
			}
		} else if (id == 5) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow + 1 }; int[] cols = { refcol,
				 * refcol + 1, refcol + 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow + 1, refcol + 1);
				this.occupied[3] = new Space(refrow + 1, refcol + 2);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() - 1;
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2 }; int[] cols = {
				 * refcol, refcol - 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 1, refcol - 1);
				this.occupied[3] = new Space(refrow + 2, refcol - 1);
			}
		} else if (id == 6) {
			if (rotationState == 0) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum();
				/*
				 * int[] rows = { refrow, refrow - 1 }; int[] cols = { refcol,
				 * refcol + 1, refcol + 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol + 1);
				this.occupied[2] = new Space(refrow, refcol + 2);
				this.occupied[3] = new Space(refrow - 1, refcol + 1);
			} else if (rotationState == 1) {
				int refrow = refSpace.getRownum() - 2;
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow + 1, refrow + 2 }; int[] cols = {
				 * refcol, refcol + 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow + 1, refcol);
				this.occupied[2] = new Space(refrow + 1, refcol + 1);
				this.occupied[3] = new Space(refrow + 2, refcol);
			} else if (rotationState == 2) {
				int refrow = refSpace.getRownum() - 1;
				int refcol = refSpace.getColnum() + 2;
				/*
				 * int[] rows = { refrow, refrow + 1 }; int[] cols = { refcol,
				 * refcol - 1, refcol - 2 }; if (!insideBounds(rows, cols)) {
				 * return false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow, refcol - 1);
				this.occupied[2] = new Space(refrow, refcol - 2);
				this.occupied[3] = new Space(refrow + 1, refcol - 1);
			} else if (rotationState == 3) {
				int refrow = refSpace.getRownum();
				int refcol = refSpace.getColnum() + 1;
				/*
				 * int[] rows = { refrow, refrow - 1, refrow - 2 }; int[] cols = {
				 * refcol, refcol - 1 }; if (!insideBounds(rows, cols)) { return
				 * false; }
				 */
				this.occupied = new Space[SPACES_PER_BLOCK];
				this.occupied[0] = new Space(refrow, refcol);
				this.occupied[1] = new Space(refrow - 1, refcol);
				this.occupied[2] = new Space(refrow - 1, refcol - 1);
				this.occupied[3] = new Space(refrow - 2, refcol);
			}
		}
		return true;
	}

	public void rotate() {
		if (id == 0) {
			/*
			 * // rotation successful if (this.buildFromRef()) {
			 * ++rotationState; if (rotationState > 1) { rotationState = 0; } }
			 */
			// TODO buildFromRef(id, rotationState + i), if successful
			// ++rotationState
			++rotationState;
			if (rotationState > 1) {
				rotationState = 0;
			}
			this.buildFromRef();
		} else if (id == 1) {
			// do nothing
		} else if (id == 2) {
			++rotationState;
			if (rotationState > 3) {
				rotationState = 0;
			}
			this.buildFromRef();
		} else if (id == 3) {
			++rotationState;
			if (rotationState > 3) {
				rotationState = 0;
			}
			this.buildFromRef();
		} else if (id == 4) {
			++rotationState;
			if (rotationState > 1) {
				rotationState = 0;
			}
			this.buildFromRef();
		} else if (id == 5) {
			++rotationState;
			if (rotationState > 1) {
				rotationState = 0;
			}
			this.buildFromRef();
		} else if (id == 6) {
			++rotationState;
			if (rotationState > 3) {
				rotationState = 0;
			}
			this.buildFromRef();
		}
	}

	/**
	 * @return Returns the uNIQUE_IDS.
	 */
	public static int getUNIQUE_IDS() {
		return UNIQUE_IDS;
	}

	/**
	 * @return Returns the sPACES_PER_BLOCK.
	 */
	public static int getSPACES_PER_BLOCK() {
		return SPACES_PER_BLOCK;
	}

	/**
	 * @return Returns the rotationState.
	 */
	public int getRotationState() {
		return rotationState;
	}

	/**
	 * @param rotationState
	 *            The rotationState to set.
	 */
	public void setRotationState(int rotationState) {
		this.rotationState = rotationState;
	}

	/**
	 * @return Returns the refSpace.
	 */
	public Space getRefSpace() {
		return refSpace;
	}

	/**
	 * @param refSpace
	 *            The refSpace to set.
	 */
	public void setRefSpace(Space refSpace) {
		this.refSpace = refSpace;
	}
}

