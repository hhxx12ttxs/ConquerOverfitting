final UsuarioDependenciaId other = (UsuarioDependenciaId) obj;
if (tbDatabase == null) {
if (other.tbDatabase != null) {
return false;
}
} else if (!tbDatabase.equals(other.tbDatabase)) {
return false;
}
if (tbDatabaseAddress == null) {

