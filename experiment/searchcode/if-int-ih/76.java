private static final long serialVersionUID = 4939965573936108738L;
private InstructionHandle ih;
private int src_line;
* @param ih instruction handle to reference
*/
public LineNumberGen(InstructionHandle ih, int src_line) {
setInstruction(ih);

