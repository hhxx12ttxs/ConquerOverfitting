private final float value;
private final String name;

private JavaVersion(float value, String name)
{
this.value = value;
this.name = name;
}

public boolean atLeast(JavaVersion requiredVersion)

