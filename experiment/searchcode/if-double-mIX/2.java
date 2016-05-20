package prclqz.methods;

import java.util.HashMap;

import prclqz.DAO.IDAO;
import prclqz.core.Message;
import static prclqz.core.Const.*;
import java.util.Map;
import prclqz.DAO.Bean.BabiesBornBean;
import prclqz.DAO.Bean.BornXbbBean;
import prclqz.DAO.Bean.TempVariablesBean;
import prclqz.core.enumLib.CX;
import prclqz.core.enumLib.HunpeiField;
import prclqz.core.enumLib.NY;
/**
 * ????????????
 * @author prclqz@zju.edu.cn
 *
 */
public class MBabiesCassificationByFuturePolicy implements IBabiesBornByFutureMethod {

	@Override
	public void calculate(IDAO m, HashMap<String, Object> globals)
			throws Exception {
		
		//get variables from globals
		TempVariablesBean tempVar = (TempVariablesBean) globals.get("tempVarBean");
		HashMap<String,Object> predictVarMap = (HashMap<String, Object>) globals.get("predictVarMap");
		HashMap<String,BornXbbBean> xbbMap = (HashMap<String, BornXbbBean>) globals.get("bornXbbBeanMap");
		
		//define local variables
		double NB2 = 0;
		BabiesBornBean bB = tempVar.getBabiesBorn();
		BabiesBornBean bBornBean = bB;
		int dqx = tempVar.getProvince();
		Map<CX,Map<NY,Map<HunpeiField,double[]>>> ziNv = (Map<CX, Map<NY, Map<HunpeiField, double[]>>>) predictVarMap.get( "HunpeiOfCXNY"+dqx );//provincMigMap.get(""+dqx);
		int X;
		int year = tempVar.getYear();
		Map<HunpeiField, double[]> zn = ziNv.get(tempVar.getCx()).get(tempVar.getNy());
		BornXbbBean xbb = xbbMap.get(""+dqx);
		
//		System.out.println("xbb:"+xbb.getXbb()+" dqx"+dqx+" xbb05-"+xbb.getBornXbb05()+" xbb90-"+xbb.getBornXbb90());
		
		//begin
		switch(tempVar.getPolicy()){
		case xianXing:
//			xbb.setXbb(1.143700);
			//////////////translated by Foxpro2Java Translator successfully:///////////////
			NB2 = bB.DD2 + bB.NMDF2 + bB.NFDM2 + bB.NN2;
//			for (X = MAX_AGE - 1; X >= 0; X--) {
				X = 0;
				if (true) {
					zn.get(HunpeiField.DDBS)[X] = 0.0;
					zn.get(HunpeiField.DDBM)[X] = 0.0 * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DDBF)[X] = 0.0 / (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.D)[X] = bB.DD1 + bB.NMDF1 + bB.NFDM1
							+ bB.NN1;
					zn.get(HunpeiField.DM)[X] = zn.get(HunpeiField.D)[X]
							* xbb.getXbb() / (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DF)[X] = zn.get(HunpeiField.D)[X]
							/ (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.N)[X] = NB2;
					zn.get(HunpeiField.NM)[X] = NB2 * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.NF)[X] = NB2 / (1.0 + xbb.getXbb());
				}
//			}
			/////////////end of translating, by Foxpro2Java Translator///////////////

			break;
		case danDu:
			// ////////////translated by Foxpro2Java Translator
			// successfully:///////////////
			NB2 = bB.getDD2() + bB.getSingleB2() + bB.getNN2();
//			for (X = MAX_AGE - 1; X >= 0; X--) {
				X = 0;
				if (true) {
					zn.get(HunpeiField.DDBS)[X] = 0.0;
					zn.get(HunpeiField.DDBM)[X] = 0.0 * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DDBF)[X] = 0.0 / (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.D)[X] = bB.DD1 + bBornBean.NMDF1
							+ bBornBean.NFDM1 + bBornBean.NN1;
					zn.get(HunpeiField.DM)[X] = zn.get(HunpeiField.D)[X]
							* xbb.getXbb() / (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DF)[X] = zn.get(HunpeiField.D)[X]
							/ (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.N)[X] = NB2;
					zn.get(HunpeiField.NM)[X] = NB2 * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.NF)[X] = NB2 / (1.0 + xbb.getXbb());
				}
//			}
			// ///////////end of translating, by Foxpro2Java
			// Translator///////////////
			break;
		case danDuHou:
			NB2 = bBornBean.NN2;
			double MIX,
			SNM_F,
			SDM_F,
			s_singleDJ,
			s_yi2Single;
//			for (X = MAX_AGE - 1; X >= 0; X--) {
				X = 0;
				if (true) {
					zn.get(HunpeiField.DDBS)[X] = bBornBean.DB
							+ bBornBean.NMDF1 + bBornBean.NFDM1
							+ bBornBean.singleB2;
					zn.get(HunpeiField.DDBM)[X] = (bBornBean.DB
							+ bBornBean.NMDF1 + bBornBean.NFDM1 + bBornBean.singleB2)
							* xbb.getXbb() / (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DDBF)[X] = (bBornBean.DB
							+ bBornBean.NMDF1 + bBornBean.NFDM1 + bBornBean.singleB2)
							/ (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.D)[X] = bBornBean.NN1;
					zn.get(HunpeiField.DM)[X] = bBornBean.NN1 * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DF)[X] = (bBornBean.NN1)
							/ (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.N)[X] = NB2;
					zn.get(HunpeiField.NM)[X] = (NB2) * xbb.getXbb()
							/ (1.0 + xbb.getXbb());
					zn.get(HunpeiField.NF)[X] = (NB2) / (1.0 + xbb.getXbb());
				}
//			}
			if (year == tempVar.getPolicyTime()) {
				MIX = 9999;
				for (X = MAX_AGE - 1; X >= 0; X--) {
					if (zn.get(HunpeiField.SingleDJ)[X] != 0.0 && X < MIX)
						MIX = X;
				}
				SDM_F = 0;
				for (X = MAX_AGE - 1; X >= 0; X--) {
					if (X > 0.0 && X <= MIX) {
						SDM_F += zn.get(HunpeiField.DM)[X]
								+ zn.get(HunpeiField.DF)[X];
					}
				}
				SNM_F = 0;
				for (X = MAX_AGE - 1; X >= 0; X--) {
					if (X > 0.0 && X <= MIX) {
						SNM_F += zn.get(HunpeiField.NM)[X]
								+ zn.get(HunpeiField.NF)[X];
					}
				}
				s_singleDJ = 0;
				s_yi2Single = 0;
				for (X = MAX_AGE - 1; X >= 0; X--) {
					if (X >= 15.0 && X <= 49.0) {
						s_singleDJ += zn.get(HunpeiField.SingleDJ)[X];
						s_yi2Single += zn.get(HunpeiField.Yi2Single)[X];
					}
				}
//				for (X = MAX_AGE - 1; X >= 0; X--) {
					X = 0;
					if (X > 0.0 && X <= MIX) {
						zn.get(HunpeiField.DDBM)[X] = zn.get(HunpeiField.DDBM)[X]
								+ s_singleDJ
								* zn.get(HunpeiField.DM)[X]
								/ SDM_F;
						zn.get(HunpeiField.DDBF)[X] = zn.get(HunpeiField.DDBF)[X]
								+ s_singleDJ
								* zn.get(HunpeiField.DF)[X]
								/ SDM_F;
					}
					if (X > 0.0 && X <= MIX) {
						zn.get(HunpeiField.DDBM)[X] = zn.get(HunpeiField.DDBM)[X]
								+ s_yi2Single
								* zn.get(HunpeiField.NM)[X]
								/ SNM_F;
						zn.get(HunpeiField.DDBF)[X] = zn.get(HunpeiField.DDBF)[X]
								+ s_yi2Single
								* zn.get(HunpeiField.NF)[X]
								/ SNM_F;
					}
					if (X > 0.0 && X <= MIX) {
						zn.get(HunpeiField.DM)[X] = zn.get(HunpeiField.DM)[X]
								- s_singleDJ * zn.get(HunpeiField.DM)[X]
								/ SDM_F;
						zn.get(HunpeiField.DF)[X] = zn.get(HunpeiField.DF)[X]
								- s_singleDJ * zn.get(HunpeiField.DF)[X]
								/ SDM_F;
					}
					if (X > 0.0 && X <= MIX) {
						zn.get(HunpeiField.NM)[X] = zn.get(HunpeiField.NM)[X]
								- s_yi2Single * zn.get(HunpeiField.NM)[X]
								/ SNM_F;
						zn.get(HunpeiField.NF)[X] = zn.get(HunpeiField.NF)[X]
								- s_yi2Single * zn.get(HunpeiField.NF)[X]
								/ SNM_F;
					}
//				}
			}
			// ///////////end of translating, by Foxpro2Java
			// Translator///////////////

			break;// break danDuHou
		case puEr:
			// ////////////translated by Foxpro2Java Translator
			// successfully:///////////////
			NB2 = 0.0;
//			for (X = MAX_AGE - 1; X >= 0; X--) {
				X = 0;
				if (true) {
					zn.get(HunpeiField.DDBS)[X] = bBornBean.DB
							+ bBornBean.NMDF1 + bBornBean.NFDM1
							+ bBornBean.singleB2 + bBornBean.NN1
							+ bBornBean.NN2;
					zn.get(HunpeiField.DDBM)[X] = zn.get(HunpeiField.DDBS)[X]
							* xbb.getXbb() / (1.0 + xbb.getXbb());
					zn.get(HunpeiField.DDBF)[X] = zn.get(HunpeiField.DDBS)[X]
							/ (1.0 + xbb.getXbb());
				}
				if (true) {
					zn.get(HunpeiField.D)[X] = 0.0;
					zn.get(HunpeiField.DM)[X] = 0.0;
					zn.get(HunpeiField.DF)[X] = 0.0;
				}
				if (true) {
					zn.get(HunpeiField.N)[X] = 0.0;
					zn.get(HunpeiField.NM)[X] = 0.0;
					zn.get(HunpeiField.NF)[X] = 0.0;
				}
//			}
			// ///////////end of translating, by Foxpro2Java
			// Translator///////////////

			break;
		default:
			break;
		}// end -switch
		
//		System.out.println(""+NB2);
	}

	@Override
	public Message checkDatabase(IDAO m, HashMap<String, Object> globals)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParam(String params, int type) throws MethodException {
		// TODO Auto-generated method stub

	}

}

