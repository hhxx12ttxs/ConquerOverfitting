else if (s.length() < 2) return s.length();

// assuming ASCII
int[] seenAt = new int[256];
for (int i = 0; i < 256; i++) {
seenAt[i] = -1; // this list will help us find the start of new non-repeating substrings once seen duplicates
}
seenAt[s.charAt(0)] = 0;
int lastUpdate = 0, max = 0, length = 0, seenIndex; // lastUpdate stands for the start of the new substring

