public int compare(final IEntity pEntityA, final IEntity pEntityB) {
return pEntityA.getZIndex() - pEntityB.getZIndex();
private static ZIndexComparator sInstance;
public static IEntityComparator instance() {
if (sInstance == null)
sInstance = new ZIndexComparator();

return sInstance;
}

}

