positions.add(newPos);
if( state.getPieceAt(newPos) != null)
break;
newPos = new Position(++col, ++row);
positions.add(newPos);
if( state.getPieceAt(newPos) != null)
break;
newPos = new Position(--col, ++row);

