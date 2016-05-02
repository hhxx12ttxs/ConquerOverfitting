package org.dtree.apps.malnut.reporting.statsources;

import java.util.Date;
import java.util.Map;

import org.dtree.emrs.EMRSPatient;
import org.dtree.emrs.EMRSPatientModel;
import org.dtree.emrs.impl.PatientInClinic;

public class Enrolled1To2   {
	public double getStat(Map<String, PatientInClinic> patientMap, Date d1,
			Date d2) {

		double count = 0.0;
		for (String key : patientMap.keySet()) {
			PatientInClinic pc = patientMap.get(key);
			EMRSPatientModel m = pc.getPatient();
			EMRSPatient p = m.getPatient();
			if (new Date(p.getCreated()).after(d1)
					&& new Date(p.getCreated()).before(d2)) {

				int months = m.getAgeInMonthsOn(new Date(p.getCreated()));
				if (months > 12 && months < 25)
					count++;
			}
		}
		return count;
	}

}

