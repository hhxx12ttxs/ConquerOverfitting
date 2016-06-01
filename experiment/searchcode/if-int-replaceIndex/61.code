packetsHandled.add(EnumPackets.ReplaceMove);
}

@Override
public void handlePacket(int index, Player pl, DataInputStream dataStream) throws IOException {
int moveToLearnIndex = dataStream.readInt();
int replaceIndex = dataStream.readInt();

EntityPlayerMP player = (EntityPlayerMP) pl;
Attack a = DatabaseMoves.getAttack(moveToLearnIndex);

