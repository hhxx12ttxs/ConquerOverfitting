package com.medicapital.server.access.gwtrpc;

import java.util.List;

import com.medicapital.common.commands.doctor.work.SelectWorkHoursCommand;
import com.medicapital.common.commands.doctor.work.SelectWorkHoursCountCommand;
import com.medicapital.common.commands.entity.SelectCommand;
import com.medicapital.common.commands.entity.SelectCommandResp;
import com.medicapital.common.commands.entity.SelectCountCommand;
import com.medicapital.common.commands.entity.SelectCountCommandResp;
import com.medicapital.common.dao.ServerException;
import com.medicapital.common.entities.WorkHours;
import com.medicapital.server.logic.WorkHoursFacade;

public class WorkHoursCommandExecutor extends CommandExecutor<WorkHours> {

	private static final long serialVersionUID = 7117560495269077366L;
	private WorkHoursFacade workHoursFacade;

	@Override
	protected SelectCommandResp<WorkHours> handleSelectCommand(SelectCommand<WorkHours> selectCommand) throws CommandExecutionException, ServerException {
		if (selectCommand instanceof SelectWorkHoursCommand) {
			return handleSelectWorkHoursCommand((SelectWorkHoursCommand) selectCommand);
		}
		return super.handleSelectCommand(selectCommand);
	}

	private SelectCommandResp<WorkHours> handleSelectWorkHoursCommand(SelectWorkHoursCommand selectCommand) throws CommandExecutionException {
		List<WorkHours> doctorWorkHours = workHoursFacade.getDoctorWorkHours(selectCommand.getDoctorId(), selectCommand.getDateFrom(), selectCommand.getDateTo(), selectCommand.getStartRow(), selectCommand.getRowCount());
		return new SelectCommandResp<WorkHours>(WorkHours.class, doctorWorkHours);
	}

	@Override
	protected SelectCountCommandResp<WorkHours> handleSelectCountCommand(SelectCountCommand<WorkHours> selectCountCommand) throws CommandExecutionException, ServerException {
		if (selectCountCommand instanceof SelectWorkHoursCountCommand) {
			return handleSelectWorkHoursCountCommand((SelectWorkHoursCountCommand) selectCountCommand);
		}
		return super.handleSelectCountCommand(selectCountCommand);
	}

	private SelectCountCommandResp<WorkHours> handleSelectWorkHoursCountCommand(SelectWorkHoursCountCommand selectCountCommand) throws CommandExecutionException {
		int workHoursCount = workHoursFacade.getDoctorWorkHoursCount(selectCountCommand.getDoctorId(), selectCountCommand.getDateFrom(), selectCountCommand.getDateTo());
		return new SelectCountCommandResp<WorkHours>(WorkHours.class, workHoursCount);
	}

	public void setWorkHoursFacade(WorkHoursFacade workHoursFacade) {
		setFacade(workHoursFacade);
		this.workHoursFacade = workHoursFacade;
	}

	@Override
	protected Class<WorkHours> getEntityClass() {
		return WorkHours.class;
	}

}

