int endRange = nums[0];

for (int i = 0; i < nums.length; i++) {
if (nums[i] > endRange + 1) {
if (startRange == endRange) solution.add(&quot;&quot; + endRange);
else solution.add(startRange + &quot;->&quot; + endRange);

