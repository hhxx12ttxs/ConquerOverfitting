return false;
}
if (!(obj instanceof ValueMarker)) {
return false;
}
if (this.value != ((ValueMarker) obj).value) {
return false;
}
return true;
}
}

