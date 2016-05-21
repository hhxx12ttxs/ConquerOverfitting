command.setDescription(descrip);
if (complex) {
commands.add(command);
command.setDescription(descrip);
if (complex) {
commands.add(command);
Command command = new RenameFieldCommand(this, fld, newName);
if (complex) {
commands.add(command);
Command command = new AddFieldCommand(this, fld);
commands.add(command);
if (complex) {
EditionEvent.CHANGE_TYPE_DELETE, sourceType);
if (complex){
rowEvents.add(event);

