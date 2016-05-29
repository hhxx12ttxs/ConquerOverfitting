for (int i = 0; i < stages.length; i++)
if (stages[i].equals(stage)) return i;
return -1;
for (int i = 0; i < stages.length; i++)
if (!hasCompleted(stages[i])) return false;
return true;

