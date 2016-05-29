int diff = 0;
for (String currChar : preorder.split(&quot;,&quot;)) {
if (diff == 1) { return false; }
diff = currChar.equals(&quot;#&quot;) ? diff+1 : diff-1;
}
return diff == 1;
}
}

