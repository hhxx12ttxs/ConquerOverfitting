for (/**/; argument_1_ > 1; argument_1_ >>= 1) {
if ((argument_1_ &amp; 0x1) != 0)
anLocalInt *= argument_0_;
argument_0_ *= argument_0_;
}
if (argument_1_ == 1)
return anLocalInt * argument_0_;
return anLocalInt;
}
}

