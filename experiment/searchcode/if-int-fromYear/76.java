@Column(name = &quot;FromYear&quot;, nullable = false)
private int fromYear;
@Basic(optional = false)
@NotNull
@Column(name = &quot;Version&quot;, nullable = false)
public Groups(Long id, String name, int fromYear, int version) {
this.id = id;
this.name = name;

