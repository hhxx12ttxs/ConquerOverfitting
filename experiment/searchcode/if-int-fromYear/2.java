public static Period fromYear(int year) {
if(year < 1789) {
return Early;
} else {
return Late;
}
}
}

