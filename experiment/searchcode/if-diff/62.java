long diffYears   = diff / (60 * 60 * 1000 * 24 * 365);

if (diffSeconds < 1) {
return &quot;less than a second ago&quot;;
} else if (diffMinutes < 1) {
return diffSeconds + &quot; second&quot; + (diffSeconds == 1 ? &quot; &quot; : &quot;s &quot;) + &quot;ago&quot;;

