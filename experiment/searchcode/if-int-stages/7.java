public class ManyStageInputProcessor implements InputProcessor {
private final Stage[] stages;
private int i;

public ManyStageInputProcessor(Stage ... stages) {
public boolean keyDown(int keycode) {
for (i=0; i<stages.length; i++) {
if (stages[i].keyDown(keycode)) {

