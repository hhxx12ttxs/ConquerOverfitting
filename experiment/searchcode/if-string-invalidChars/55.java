// char s[] = inputString.toCharArray();
Stack<Integer> st = new Stack<>();
Set<Integer> invalidChars = Sets.newHashSet();
StringBuilder sb = new StringBuilder();
for (int i = 0; i < r.length; i++) {
if (!invalidChars.contains(i)) {
sb.append(r[i]);
}
}
return sb.toString();
}
}

