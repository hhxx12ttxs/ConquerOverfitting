boolean firstChar = true;

for (char c : input.toCharArray()) {
if (firstChar) {
c = Character.toUpperCase(c);
firstChar = false;
} else {
c = Character.toLowerCase(c);

