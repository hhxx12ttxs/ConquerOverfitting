int numRuns = 10;
int tokenLen = 5;

int numStrings = 100000;

StringBuilder sb = new StringBuilder();
for (int numTokens = 1; numTokens <= 20; numTokens++) {
benchmark(numRuns, numStrings, numTokens, tokenLen);
}
}

public static void benchmark(int numRuns, int numStrings,

