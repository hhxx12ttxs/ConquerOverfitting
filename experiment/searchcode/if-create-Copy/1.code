public <T> T copyObject(T object)
{
if (object == null)
return null;

if (!getMustCopyInstanceOf(object.getClass()))
protected <T> CopyWorker<T> createCopyWorker(T object)
{
if (object.getClass().isArray())
return new ArrayCopyWorker<T>(this);

