if(\"simple\".equals(mode)) {
log.info(\"use simple mode\");
seg = new SimpleSeg(dic);
} else if(\"complex\".equals(mode)) {
log.info(\"use complex mode\");
seg = new ComplexSeg(dic);
} else {
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
if(tokenizer == null) {
tokenizer = newTokenizer(input);
MMSegTokenizer tokenizer = tokenizerLocal.get();
String mode = args.get(\"mode\");

