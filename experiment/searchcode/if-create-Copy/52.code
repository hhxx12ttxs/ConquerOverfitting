EntityManager entityManager=JPAUtil.getEntityManager();
if (entityManager.createNamedQuery(&quot;findAllBookCopies&quot;).getResultList().isEmpty())
return ((List<BookCopy>)entityManager.createNamedQuery(&quot;findAllBookCopies&quot;).getResultList());
}


public boolean add(BookCopy bookCopy) throws BookExistsException{

