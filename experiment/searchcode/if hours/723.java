package com.cdel.advc.plan.action;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.event.RowEditEvent;

import com.cdel.advc.plan.domain.MemberTermHours;
import com.cdel.advc.plan.facade.MemberTermHoursFacade;
import com.cdel.util.BaseAction;

/**
 * 教师设定学习时间
 * 
 * @author 张苏磊
 * 
 */
@SuppressWarnings("serial")
@ManagedBean
public class MemberTermHoursReqAction extends BaseAction<MemberTermHours> {

	@ManagedProperty("#{memberTermHoursFacade}")
	private MemberTermHoursFacade memberTermHoursFacade;

	private byte submitSuccess = 0;// 添加是否成功

	/**
	 * 更新教师时间设置
	 * 
	 * @throws Exception
	 */
	public void updateTime(RowEditEvent event) {
		submitSuccess = -1;
		MemberTermHours hours = (MemberTermHours) this.getEditRow(event);
		if (hours == null || hours.getUserID() == null
				|| hours.getMthType() == null || hours.getTermID() == null) {
			this.addWarnMessage("info", "非法参数!");
		}
		if (memberTermHoursFacade.checkHour(hours)) {
			hours.setTotalSum((short) 0);
			hours.forMinites();
			try {
				if (hours.getMthID() == null) {
					this.memberTermHoursFacade.add(hours, hours.getPlanID(),
							this.getCurrentUserID(), this.getCurrentRealName());
				} else {
					this.memberTermHoursFacade.update(hours, hours.getPlanID(),
							this.getCurrentUserID(), this.getCurrentRealName());
				}
				PlanChapterAction pa = (PlanChapterAction) this
						.getViewAction("planChapterAction");
				pa.setReGeneratePlan(true);
				this.addInfoMessage("info", SUCESSINFO);
				submitSuccess = 1;
			} catch (Exception e) {
				e.printStackTrace();
				submitSuccess = -1;
				this.addErrorMessage("info", FAILINFO);
			}
		}
		this.addCallbackParam("result", submitSuccess);
	}

	public void setMemberTermHoursFacade(
			MemberTermHoursFacade memberTermHoursFacade) {
		this.memberTermHoursFacade = memberTermHoursFacade;
	}

}

