/**
 * 
 */
package org.dualr.litelog.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import org.dualr.litelog.dao.PostDao;
import org.dualr.litelog.entity.Category;
import org.dualr.litelog.entity.Post;
import org.dualr.litelog.utils.PostStatus;

/**
 * @author rick
 * 
 */
public class PostService {
	private PostDao postDao;
	private Cache cache;
	private static PostService postService = null;

	private PostService() {
		postDao = new PostDao();
		try {
			CacheFactory cacheFactory = CacheManager.getInstance()
					.getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			cache = null;
		}
	}

	public static PostService getInstance() {
		if (postService == null) {
			postService = new PostService();
		}
		return postService;
	}

	public void add(Post post) {
		// update tag
		// update archive
		TagService.getInstance().save(post.getTag());
		this.postDao.save(post);
	}

	public List<Post> list() {
		return this.postDao.getAll();
	}

	public List<Post> list(int index, int offset) {
		return this.postDao.getByRange(index, offset);
	}

	public void delete(Long id) {
		Post post = this.getById(id);
		for (String tagName : post.getTag()) {
			TagService.getInstance().save(tagName, -1);
		}
		this.postDao.deleteById(id);
	}

	public void save(Post post) {
		Post p = this.getById(post.getId());
		TagService.getInstance().update(p.getTag(), post.getTag());
		this.postDao.update(post);
	}

	public Post getById(Long id) {
		return this.postDao.getById(id);
	}

	public List<Post> getList(int index, int offset) {
		return this.postDao.getByRange(index, offset);
	}

	public List<Post> getList(PostStatus ps, int index, int offset) {
		return this.postDao.getByStatusRange(ps.getValue(), index, offset);
	}

	public List<Post> getByTag(String tag, int index, int offset) {
		return this.postDao.getByTag(tag, index, offset);
	}

	public List<Post> getByCategoryId(long categoryId, int index, int offset) {
		return this.postDao.getByCategoryId(categoryId, index, offset);
	}

	public List<Post> getByDate(Date start, Date end, int index, int offset) {

		return null;
	}

	public int getCount() {
		String sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post";
		Integer value = (Integer) this.postDao.GetByQueryString(sql);
		return value;
	}

	/**
	 * 
	 * <pre>
	 * 1.publish
	 * 2.draft
	 * 3.private
	 * </pre>
	 * 
	 * @param status
	 * @return
	 */
	public int getCount(PostStatus ps) {
		String sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post where status == '"
				+ ps.getValue() + "'";
		Integer value = (Integer) this.postDao.GetByQueryString(sql);
		return value;
	}

	public int getCountByTag(String tag) {
		String sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post where tag == '"
				+ tag + "'";
		Integer value = (Integer) this.postDao.GetByQueryString(sql);
		return value;
	}

	public int getCountByCategoryId(long categoryId) {
		String sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post where categoryId == "
				+ categoryId;
		Integer value = (Integer) this.postDao.GetByQueryString(sql);
		return value;
	}

	/**
	 * @param id
	 * @param i
	 */
	public void updateHits(Long id, int size) {
		this.postDao.updateHits(id, size);

	}

	/**
	 * @param parseLong
	 * @param i
	 */
	public void updateCommentCount(long id, int size) {
		this.postDao.updateCommentCount(id, size);
	}

	/**
	 * @return
	 */
	public int getCountByCategory(String category) {
		Category c = CategoryService.getInstance().getByName(category);
		return this.getCountByCategoryId(c.getId());
	}

	/**
	 * @return
	 */
	public int getCountByCategory(String category, PostStatus status) {
		Category c = CategoryService.getInstance().getByName(category);
		if (c == null) {
			return 0;
		} else {
			return this.getCountByCategoryId(c.getId(), status);
		}
	}

	/**
	 * @param id
	 * @param status
	 * @return
	 */
	private int getCountByCategoryId(Long categoryId, PostStatus status) {
		String sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post where categoryId == "
				+ categoryId + " && status == '" + status.getValue() + "'";
		return (Integer) this.postDao.GetByQueryString(sql);
	}

	/**
	 * @param category
	 * @param i
	 * @param pageSize
	 * @return
	 */
	public List<Post> getByCategoryName(String category, int index, int offset) {
		Category c = CategoryService.getInstance().getByName(category);
		return this.getByCategoryId(c.getId(), index, offset);
	}

	/**
	 * @param category
	 * @param i
	 * @param pageSize
	 * @return
	 */
	public List<Post> getByCategoryName(String category, PostStatus status,
			int index, int offset) {
		Category c = CategoryService.getInstance().getByName(category);
		if (c == null) {
			return null;
		} else {
			return this.getByCategoryId(c.getId(), status, index, offset);
		}
	}

	/**
	 * @param id
	 * @param status
	 * @param index
	 * @param offset
	 * @return
	 */
	public List<Post> getByCategoryId(Long categoryId, PostStatus status,
			int index, int offset) {
		return this.postDao.getByCategoryId(categoryId, status, index, offset);
	}

	public List<Post> getPopPost(int index, int offset) {
		return this.postDao.getPopPost(index, offset);
	}

}

