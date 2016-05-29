if (invShow) {
i1 = inv2.getHeight();
} else {
i1 = 0;
}

if (invY != i1) {
invY = invY + (i1 - invY) / 2;

if ((i1 - invY) / 2 == 0) {
invY = i1;
}
}

if (health != entity.getHealth()) {

