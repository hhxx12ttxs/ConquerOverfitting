max[maxLength++] = i;
}
int answer = 0;
if (levels[max[maxStart]] - levels[min[minStart]] == length - 1)
answer++;
for (int i = length; i < count; i++) {
if (max[maxStart] == i - length)

