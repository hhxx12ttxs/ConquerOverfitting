l1.add(&#39;a&#39;);
l1.add(&#39;r&#39;);
l1.add(&#39;f&#39;);
Stack<Character> s=new Stack<Character>();
for(int i=0;i<l1.size();i++){s.push(l1.get(i));}System.out.println(s);
for(int i=0;i<l1.size();i++){if(s.pop()==l1.get(i))System.out.println(&quot;yes its a palindrome&quot;);else System.out.println(&quot;not a palindrome&quot;);}

