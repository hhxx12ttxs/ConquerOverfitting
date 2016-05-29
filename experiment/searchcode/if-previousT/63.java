System.out.print(&quot;\nType &#39;e&#39; to encrypt or &#39;d&#39; to decrypt: &quot;);

input = keyboard.nextLine().charAt(0);

if(input == &#39;e&#39;)
{
System.out.print(&quot;Enter plaintext: &quot;);
System.out.println(&quot;\n\nCiphertext: &quot; + cipherText);
}
else if(input == &#39;d&#39;)
{
System.out.print(&quot;Enter ciphertext: &quot;);

