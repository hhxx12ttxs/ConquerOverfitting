zobristPawn = computeZobrist(this, PAWN) ^ computeZobrist(this, KING);
repetitionTable.clear();
repetitionTable.increment(zobrist);

