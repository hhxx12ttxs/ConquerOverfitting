int lineheight = tableLineHeight();
int line = y / lineheight;
if (line < 0) line = 0;
if (line >= lns.length-1) line=lns.length-2;
Dispatch.dispatch(receipt, ndata);
}

void moveSelection(int nsel) {
// do nothing for one line selection
if (lns.length-1 == 1) return;

