private final String id;

public TargetSiteId(String id) {
assert id != null;
this.id = id;
}

@Override
public boolean equals(Object o) {
if (this == o) return true;

