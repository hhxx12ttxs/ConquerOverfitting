r0 = r3 &amp; 255;
r3 = r0 &amp; 192;
r4 = 192; // 0xc0 float:2.69E-43 double:9.5E-322;
if (r3 != r4) goto L_0x0031;
L_0x000a:
r3 = r0 &amp; 63;
r3 = r3 << 8;
r4 = r6 + 1;

