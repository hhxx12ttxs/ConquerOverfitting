public class LineNumberGen implements InstructionTargeter, Cloneable {

private InstructionHandle ih;
private int src_line;


/**
* Create a line number.
public LineNumberGen(InstructionHandle ih, int src_line) {
setInstruction(ih);
setSourceLine(src_line);

