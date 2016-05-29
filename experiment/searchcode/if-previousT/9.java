private JavaSource _source;

private boolean _farAway = false;

private String _previoustWord;
private String _currentWord;
public void handleWord(String sval) {

_previoustWord = _currentWord;
_currentWord = sval;
//System.out.print(&quot;w:&quot;+sval+&quot; &quot;);

if(_farAway) return;

