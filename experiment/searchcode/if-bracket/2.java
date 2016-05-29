Stack<Character> stack = new Stack<>();

for (char bracket : bracketSet.toCharArray()) {
if (isOpeningBracket(bracket)) {
stack.push(bracket);
} else if (isClosingBracket(bracket)) {
if (stack.empty() || !stack.peek().equals(opening(bracket)))

