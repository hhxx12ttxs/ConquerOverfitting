int ncomponents = parent.getComponentCount();
int nrows = 2;
int ncols = 3;
nrows = (ncomponents + ncols - 1) / ncols;
}
int w = parent.getWidth() - (insets.left + insets.right);

