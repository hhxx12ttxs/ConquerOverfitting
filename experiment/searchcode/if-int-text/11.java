public NumberTextField(int text){
super(&quot;&quot;+text);
}

@Override
public void replaceText(int start, int end, String text){
if (validate(text)){
super.replaceText(start, end, text);

