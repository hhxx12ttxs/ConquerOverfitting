if(currChar == &#39;(&#39; || currChar == &#39;{&#39; || currChar == &#39;[&#39;) bracketQ.push(currChar);
else if(currChar == &#39;)&#39;){
if(bracketQ.size() == 0 || !bracketQ.peek().equals(&#39;(&#39;)) return false;
else bracketQ.pop();
}else if(currChar == &#39;}&#39;){
if(bracketQ.size() == 0 || !bracketQ.peek().equals(&#39;{&#39;)) return false;

