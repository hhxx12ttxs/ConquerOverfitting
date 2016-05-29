// Update the number of times production goals were met for
// Shift supervisors
if (e instanceof ShiftSupervisor) {
ShiftSupervisor supervisor = (ShiftSupervisor) e;
if (s.goalsMet()) {

