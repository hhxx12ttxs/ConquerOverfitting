csBuff.writeByte(((Boolean)instr.getAttachment()).booleanValue() ? (byte)1 : (byte)0);
}
int csLength = csBuff.getPosition();
int switchBlocksSize = 0; // TODO implement switch blocks encoding

csBuff.writeInt(csLength);

