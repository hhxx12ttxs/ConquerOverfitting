public static final String R_REFS = &quot;refs/&quot;;

/** Branch name key */
public static class NameKey extends StringKey<Project.NameKey> {
protected Project.NameKey projectName;

@Column(id = 2)
protected String branchName;

protected NameKey() {

