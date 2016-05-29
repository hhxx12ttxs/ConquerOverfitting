if (o == null || getClass() != o.getClass()) return false;

final SeenEntry seenEntry = (SeenEntry) o;

if (!path.equals(seenEntry.path)) return false;
return server.equals(seenEntry.server);

}

@Override
public int hashCode() {

