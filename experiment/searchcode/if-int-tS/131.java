package tap.execounting.components.grids;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.BeanModelSource;

import tap.execounting.dal.CRUDServiceDAO;
import tap.execounting.dal.ChainMap;
import tap.execounting.dal.mediators.interfaces.ClientMed;
import tap.execounting.entities.Client;
import tap.execounting.entities.Comment;
import tap.execounting.entities.Contract;
import tap.execounting.entities.ContractType;
import tap.execounting.entities.Teacher;
import tap.execounting.pages.ClientPage;
import tap.execounting.util.DateUtil;

@Import(stylesheet = {"context:css/datatable.css", "context:css/comments.css"}, library = { "context:js/jquery-1.8.3.min.js",
"context:js/comments.js" })
public class ClientGridNCD {
	@Inject
	private BeanModelSource beanModelSource;
	@Inject
	private ComponentResources componentResources;
	@Inject
	private ClientMed clientMed;
	@Inject
	private CRUDServiceDAO dao;

	@Property
	@Parameter
	private List<Client> source;

	@Property
	private boolean editorActive;

	private BeanModel<Client> model;
	@Property
	private Client unit;
	@Property
	private Contract loopContract;
	@InjectPage
	private ClientPage clientPage;

	private ClientMed getClientMed() {
		return clientMed;
	}

	public BeanModel<Client> getModel() {
		if (model == null) {
			model = beanModelSource.createDisplayModel(Client.class,
					componentResources.getMessages());
			model.exclude("id", "return", "firstPlannedPaymentDate", "date",
					"canceled", "firstContractDate");
			model.add("state", null);
			model.add("contracts", null);
			model.add("comment",null);
			model.reorder("name", "comment", "contracts");
		}
		return model;
	}

	Object onActionFromDetails(Client c) {
		clientPage.setup(c);
		return clientPage;
	}

	public String getTeachers() {
		StringBuilder sb = new StringBuilder();
		List<Teacher> ts = unit.getCurrentTeachers();
		for (int i = 0; i < ts.size(); i++) {
			sb.append(ts.get(i).getName());
			if (i < ts.size() - 1)
				sb.append(", ");
		}
		return sb.toString();
	}
	

	public Date getCommentDate() {
		Comment c = clientMed.setUnit(unit).getComment();
		return c == null ? null : c.getDate();
	}
	
	public String getComment() {
		Comment c = clientMed.setUnit(unit).getComment();
		return c == null ? "" : c.getText();
	}

	// TODO remove
	public String getFirstContractDate() {
		Date d = unit.getFirstContractDate();
		if (d == null)
			return "договоров по данному клиенту нет в базе";
		return DateUtil.toString("dd MMM YYYY", d);
	}

	public List<Contract> getContracts() {
		return getClientMed().setUnit(unit).getActiveContracts();
	}

	public String getContractInfo() {
		StringBuilder sb = new StringBuilder();
		Contract c = loopContract;
		sb.append(DateUtil.toString("dd.MM.YYYY\t", c.getDate()));
		if (c.getContractTypeId() != ContractType.Standard)
			sb.append(c.getContractType().getTitle() + ". ");

		sb.append(c.getEventType().getTitle() + " (" + c.getTeacher().getName()
				+ "). ");
		sb.append("Баланс: " + c.getBalance());

		return sb.toString();
	}

	public String getCssForBalance() {
		int balance = 0;
		Client c = dao.find(Client.class, unit.getId());
		balance = c.getBalance();

		// balance = unit.getBalance(); // good old
		if (balance < 0)
			return "debtor";
		if (balance > 0)
			return "creditor";
		return "neutral";
	}

	public String getState() {
		return getClientMed().setUnit(unit).getState().toString();
	}
	

	public JSONObject onAJpoll(@RequestParameter("timeStamp") long timestamp) {
		JSONObject js = new JSONObject("{'status':'ok'}");
		List<Comment> list = dao.findWithNamedQuery(Comment.CLIENT_AFTER_DATE,
				ChainMap.with("date", new Date(timestamp)));
		if (list.size() > 0) {
			JSONArray jr = new JSONArray();
			for (Comment c : list)
				jr.put(new JSONObject("id", c.getEntityId() + "", "comment", c
						.getText(), "timeStamp", c.getDate().getTime() + ""));
			js.put("updates", jr);
		}
		return js;
	}

	public JSONObject onAJ(@RequestParameter("id") int id,
			@RequestParameter("comment") String text,
			@RequestParameter("timeStamp") long timeStamp) {

		clientMed.setUnitById(id).comment(text, timeStamp);
		JSONObject js = new JSONObject("{'status':'ok'}");
		return js;
	}

}

