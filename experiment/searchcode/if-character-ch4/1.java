int ch4 = readByte();

if((ch3 | ch4) < 0)
throw new EOFException();

return (short) ((ch3 << 8) + (ch4 << 0));
}

public default char readCharacter() throws IOException
{
int ch3 = readByte();

