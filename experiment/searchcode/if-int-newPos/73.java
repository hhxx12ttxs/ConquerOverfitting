if(!(newPos < 0-WIDTH) &amp;&amp; !(newPos >= Screen.width) ){ // check it&#39;s not at the edge of the screen
xPosition = newPos ;
}
}
if(Keyboard.left){
int newPos = xPosition-(1*KEYBOARD_SENSITIVITY);
xPosition = newPos ;
}
}
if(Keyboard.right){
int newPos = xPosition+(1*KEYBOARD_SENSITIVITY);

