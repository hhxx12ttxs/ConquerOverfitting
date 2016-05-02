package org.dualr.litelog.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.dualr.litelog.entity.Post;
import org.dualr.litelog.utils.PostStatus;

public class PostDao extends DaoImpl<Post> {

	public Long insert(Post post) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(post);
		} finally {
			pm.close();
		}
		return post.getId();
	}

	public Post getById(Long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Post post = null;
		post = pm.getObjectById(Post.class, id);
		return post;
	}

	@SuppressWarnings("unchecked")
	public Post getByPostId(Long id) {
		List<Post> postList = null;
		Post post = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setFilter("id == idParam");
		query.declareParameters("Long idParam");
		try {
			postList = (List<Post>) query.execute(id);
			if (postList.size() != 0) {
				post = postList.get(0);
			}
		} finally {
			query.closeAll();
		}
		return post;
	}

	// update post
	public void update(Post post) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Post p = pm.getObjectById(Post.class, post.getId());
			p.setTitle(post.getTitle());
			p.setCategoryId(post.getCategoryId());
			p.setDescription(post.getDescription());
			p.setContent(post.getContent());
			p.setImage(post.getImage());
			p.setStatus(post.getStatus());
			p.setTag(post.getTag());
			// p.setPostDate(post.getPostDate());
			p.setCommentStatus(post.getCommentStatus());
		} finally {
			pm.close();
		}
	}

	// get post list by range(page)
	@SuppressWarnings("unchecked")
	public List<Post> getByRange(int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setRange(index, index + offset);
		query.setOrdering("postDate desc");
		List<Post> postList = null;
		try {
			postList = (List<Post>) query.execute();
			return postList;
		} finally {
			query.closeAll();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Post> getByStatusRange(String status, int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setRange(index, index + offset);
		query.setFilter("status == statusParam");
		query.declareParameters("String statusParam");
		query.setOrdering("postDate desc");
		List<Post> postList = null;
		try {
			postList = (List<Post>) query.execute(status);
			return postList;
		} finally {
			query.closeAll();
		}
	}

	// get popular post list
	@SuppressWarnings("unchecked")
	public List<Post> getPopPost(int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setRange(index, index + offset);
		query.setOrdering("hits desc");
		List<Post> postList = null;
		try {
			postList = (List<Post>) query.execute();
			return postList;
		} finally {
			query.closeAll();
		}
	}

	// update hits or commentCount
	public void updateHits(Long id, int size) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Post post = pm.getObjectById(Post.class, id);
			post.setHits(post.getHits() + size);
		} finally {
			pm.close();
		}
	}

	// update hits or commentCount
	public void updateCommentCount(Long id, int size) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Post post = pm.getObjectById(Post.class, id);
			post.setCommentCount(post.getCommentCount() + size);
		} finally {
			pm.close();
		}
	}

	// get post title by id
	public String getTitleById(Long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Post post = pm.getObjectById(Post.class, id);
		return post.getTitle();
	}

	// get post count by categoryId or tag or all
	public int getCountByPropertyStatus(String parameter, String value,
			String status) {
		String sql = null;
		int i;
		sql = "SELECT count(id) FROM org.dualr.litelog.entity.Post WHERE status == '"
				+ status + "'";
		PersistenceManager pm = PMF.get().getPersistenceManager();
		if (parameter.equals("categoryId")) {
			sql = sql + " && categoryId == " + value;
			Query query = pm.newQuery(sql);
			try {
				i = (Integer) query.execute();
			} finally {
				query.closeAll();
			}
		} else if (parameter.equals("tag")) {
			sql = sql + " && tag == '" + value + "'";
			Query query = pm.newQuery(sql);
			try {
				i = (Integer) query.execute();
			} finally {
				query.closeAll();
			}
		} else {
			Query query = pm.newQuery(sql);
			try {
				i = (Integer) query.execute();
			} finally {
				query.closeAll();
			}
		}
		return i;
	}

	@SuppressWarnings("unchecked")
	public List<Post> getPostByPropertyStatus(String property, String value,
			String status, int indexStart, int indexEnd) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String sql = "SELECT FROM org.dualr.litelog.entity.Post WHERE status == '"
				+ status + "'";
		if (property.equals("categoryId")) {
			sql = sql + " && categoryId == " + value;
		} else {
			sql = sql + " && tag == '" + value + "'";
		}
		Query query = pm.newQuery(sql);
		List<Post> postList = null;
		query.setRange(indexStart, indexEnd);
		query.setOrdering("postDate desc");
		try {
			postList = (List<Post>) query.execute();
		} finally {
			query.closeAll();
		}
		return postList;
	}

	/**
	 * @param tag
	 * @param index
	 * @param offset
	 * @return
	 */
	public List<Post> getByTag(String tag, int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setFilter("tag == tagStr");
		query.setOrdering("postDate desc");
		query.declareParameters("String tagStr");
		query.setRange(index, index + offset);
		List<Post> postList;
		try {
			postList = (List<Post>) query.execute(tag);
		} finally {
			query.closeAll();
		}
		return postList;
	}

	public List<Post> getByCategoryId(Long categoryId, int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setFilter("categoryId == categoryIdParam");
		query.setOrdering("postDate desc");
		query.declareParameters("Long categoryIdParam");
		query.setRange(index, index + offset);
		List<Post> postList;
		try {
			postList = (List<Post>) query.execute(categoryId);
		} finally {
			query.closeAll();
		}
		return postList;
	}

	/**
	 * @param categoryId
	 * @param status
	 * @param index
	 * @param offset
	 * @return
	 */
	public List<Post> getByCategoryId(Long categoryId, PostStatus status,
			int index, int offset) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Post.class);
		query.setFilter("categoryId == categoryIdParam && status == statusStr");
		query.declareParameters("Long categoryIdParam, String statusStr");
		query.setOrdering("postDate desc");
		query.setRange(index, index + offset);
		List<Post> postList;
		try {
			postList = (List<Post>) query.execute(categoryId, status.getValue());
		} finally {
			query.closeAll();
		}
		return postList;
	}

}

