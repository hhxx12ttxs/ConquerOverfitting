protected <T> CopyWorker<T> createCopyWorker(T object)
{
if (object.getClass().isAssignableFrom(classToInstantiate))
{
return new HibernateObjectSubclassBeanCopyWorker<T>(this, (Class<T>) classToInstantiate);
}
return super.createCopyWorker(object);
}

}

