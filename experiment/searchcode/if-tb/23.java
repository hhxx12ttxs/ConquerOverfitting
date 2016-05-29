TbArticleDAO TbArticleDAO = new TbArticleDAO();
TbArticleDAO.save(newArticle);
}

public void saveArticle(TbArticle article) {
if (article != null) {
TbArticleDAO.update(article);
}
}

public void deleteArticle(TbArticle article) {
if (article != null) {

