private int years;
private String id;

private Bond(Builder b) {
this.years = b.years;
this.id = b.id;
}

public static class Builder
{
private int years;

