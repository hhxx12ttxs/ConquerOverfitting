public enum Step {
A(0), B(1), C(2), D(3), E(4), F(5), G(6);

private final int stepValue;

private Step(int value) {
return Step.values()[value];
}

public static int getNoteScale(Note.Step step) {
if (step == Note.Step.A) {
return NoteScale.A;

