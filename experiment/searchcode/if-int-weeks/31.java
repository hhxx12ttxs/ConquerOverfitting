int days = seconds % 2678400 % 604800 / 86400;
int weeks = seconds % 2678400 / 604800;
int mins = seconds % 2678400 % 604800 % 86400 % 3600 / 60;
int secs = seconds % 2678400 % 604800 % 86400 % 3600 % 60;

if (months > 0)

