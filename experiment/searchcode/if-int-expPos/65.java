int expBits = expLen * BITS; // êîëè÷åñòâî áèòîâ ñòåïåíè
int expPos = expLen - 1; // ïîçèöèÿ
long bitMask = BIT_MASK; // áèòîâàÿ ìàñêà
// 2.2 If e[i] = 1 then A = Mont(A, temp).
if (0 != (e[expPos] &amp; bitMask)) // åñëè áèò ðàâåí 1

