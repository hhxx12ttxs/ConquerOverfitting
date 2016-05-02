package cn.org.act.sdp.bpmnRepository.tool;

import java.util.HashMap;
import java.util.List;

import cn.org.act.sdp.bpmnRepository.crust.impl.RepositoryConf;
import cn.org.act.sdp.bpmnRepository.entity.MetaBean;
import cn.org.act.sdp.bpmnRepository.entity.TitleBean;
import cn.org.act.sdp.bpmnRepository.session.MetaSession;
import cn.org.act.sdp.bpmnRepository.session.SessionFactory;
import cn.org.act.sdp.bpmnRepository.session.SessionType;

/**
 * Ԫ��ݱ�ǣ����̱�����?����������࣬�汾����Ķ���API
 * 
 * @author dell
 * 
 */
public class MetaTool {
	private float threshold = 0;
	private MetaSession session = null;

	/**
	 * @return
	 * @throws Exception
	 */
	private MetaSession init() {

		RepositoryConf.poolInit();
		RepositoryConf.managerInit();

		try {
			session = (MetaSession) (SessionFactory
					.openSession(SessionType.Meta));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return session;
	}

	public int save(Object o) {
		init();
		int id = session.save(o);
		session.close();
		return id;
	}

	public boolean update(Object o) {
		init();
		boolean b = session.update(o);
		session.close();
		return b;
	}

	public MetaBean getById(int id) {
		init();
		MetaBean bean = session.getById(id);
		session.close();
		return bean;
	}

	public boolean getLatestByTitle(TitleBean title) {
		if (title == null)
			return false;
		HashMap<String, MetaBean> map = title.getMetas();

		// if list is null , new one and pass to title
		if (map == null) {
			map = new HashMap<String, MetaBean>();
			title.setMetas(map);
		}

		if (title.getLatest() <= 0) {
			return true;
		}
		init();
		MetaBean bean = session.getLatestByTitle(title);
		session.close();
		// connect
		map.put("v" + 0, bean);
		if (bean != null)
			bean.setTitle(title);
		return true;
	}

	public boolean getVersionByTitle(TitleBean title, int version) {
		if (title == null)
			return false;
		HashMap<String, MetaBean> map = title.getMetas();

		// if list is null , new one and pass to title
		if (map == null) {
			map = new HashMap<String, MetaBean>();
			title.setMetas(map);
		}

		if (title.getLatest() <= 0) {
			return true;
		}
		init();
		MetaBean bean = session.getVersionByTitle(title, version);
		session.close();
		map.put("v" + version, bean);
		if (bean != null)
			bean.setTitle(title);
		return true;
	}

	public boolean remove(int id) {
		init();
		boolean b = session.remove(id);
		session.close();
		return b;
	}

	public boolean remove(MetaBean meta) {
		init();
		boolean b = session.remove(meta);
		session.close();
		return b;
	}

	public List<MetaBean> getAllVersionsByTitleId(int titleId) {
		init();
		List<MetaBean> list = session.getAllVersionsByTitleId(titleId);
		session.close();
		return list;
	}
}

