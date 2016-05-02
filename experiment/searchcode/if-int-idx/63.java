package main.DexClass;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import DAL.APKDAL;
import DAL.ClassDAL;
import DAL.FieldDAL;
import DAL.MethodDAL;
import DAL.SummaryDAL;
import utils.ClassConstants;
import utils.Utils;
import main.DexClass.Dataset.AnnotationDirectoryItem;
import main.DexClass.Dataset.AnnotationItem;
import main.DexClass.Dataset.AnnotationOff;
import main.DexClass.Dataset.AnnotationSetItem;
import main.DexClass.Dataset.AnnotationSetRefList;
import main.DexClass.Dataset.ByteCode;
import main.DexClass.Dataset.ClassDataItem;
import main.DexClass.Dataset.ClassDefs;
import main.DexClass.Dataset.CodeItem;
import main.DexClass.Dataset.DebugInfoItem;
import main.DexClass.Dataset.EncodedArray;
import main.DexClass.Dataset.EncodedCatchHandler;
import main.DexClass.Dataset.EncodedCatchHandlerList;
import main.DexClass.Dataset.EncodedField;
import main.DexClass.Dataset.EncodedMethod;
import main.DexClass.Dataset.FieldIds;
import main.DexClass.Dataset.Header;
import main.DexClass.Dataset.MapList;
import main.DexClass.Dataset.MethodIds;
import main.DexClass.Dataset.ProtoIds;
import main.DexClass.Dataset.StringDataItem;
import main.DexClass.Dataset.StringIds;
import main.DexClass.Dataset.TryItem;
import main.DexClass.Dataset.TypeIds;
import main.DexClass.Dataset.TypeItem;
import main.DexClass.Dataset.TypeList;
import handler.LogHandler;

public class DexClassPrinter extends DexClassBody{

	private Header header = null;
	private MapList maplist = null;
	private TypeList typeList = null;
	private AnnotationSetRefList asrl = null;
	private AnnotationSetItem asi = null;
	private DebugInfoItem[] dii = null;
	private AnnotationItem[] ai = null;
	private EncodedArray[] ear = null;
	private AnnotationDirectoryItem[] adi = null;
	
	private DexClassBody dcb = null;
	private LogHandler logger = null;
	private ClassDefs cds = null;
	private TypeIds tis = null;
	private StringIds sis = null;
	private StringDataItem sdi = null;
	private ClassDataItem cdi = null;
	private CodeItem ci = null;
	private MethodIds mi = null;
	private FieldIds fi = null;
	private ProtoIds pi = null;
	private SummaryDAL sdal = null;
	private int apk_id = 0;
	
	String separate = "--------------------------------------------------------------------------";
	
	List<String> result = new ArrayList<String>();
	String packageName = "";
	
	public DexClassPrinter(DexClassBody dexClassBody) {
		// TODO Auto-generated constructor stub
		dcb = dexClassBody;
		this.header = dcb.getHeader();
		this.maplist = dcb.getMaplist();
		this.typeList = dcb.getTypeList();
		this.asrl = dcb.getAsrl();
		this.asi = dcb.getAsi();
		this.dii = dcb.getDii();
		this.ai = dcb.getAi();
		this.ear = dcb.getEar();
		this.adi = dcb.getAdi();
		this.logger = dcb.getLogger();
		this.cds = dcb.getClass_defs();
		this.tis = dcb.getType_ids();
		this.sis = dcb.getString_ids();
		this.sdi = dcb.getSdi();
		this.cdi = dcb.getCdi();
		this.ci = dcb.getCodeItem();
		this.mi = dcb.getMethod_ids();
		this.fi = dcb.getField_ids();
		this.pi = dcb.getProto_ids();
	}

	public void print(){
		logger.info(dcb.getHeader().toString());
		logger.info(dcb.getMaplist().toString());
		
//		javaStat();
		parseXML();
		
		
//		classsummary();
		methodsummary();
		fieldsummary();
		createsummary();
//		this.sdal.closeConnection();
	}

	private void createsummary() {
		// TODO Auto-generated method stub
		SummaryDAL sdal = new SummaryDAL();
		this.sdal = sdal;
		sdal.insertSummary(apk_id);
	}

	private void javaStat(){
		// TODO Auto-generated method stub
				String filePath = "output";
				String fileName = "exportall.xml";
				String[] path = new String[100];
				
				File file = new File(fileName);
				if (!file.exists()){
					logger.error("exportall.xml File not Exists!!!");
					return;
				}

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(file);
					Element el = doc.getDocumentElement();
					NodeList nl = el.getElementsByTagName("Package"); // Activity Element
					for (int i=0; i<nl.getLength(); i++)
					{
						Node node = nl.item(i);
						
						NodeList nnl = node.getChildNodes();
						Node n2 = nnl.item(0);
//						((Element)n2).compareDocumentPosition(node);
						
						NamedNodeMap nnmnode = node.getAttributes();
						path[i] = nnmnode.item(0).getNodeValue();
						Element panode = (Element) node;
						NodeList snl = panode.getElementsByTagName("Package");
						Element eee = (Element)n2;
						eee.getElementsByTagName("Package");
						Node nnn = snl.item(0);
						Element stnode = (Element) nnn;
						NodeList vnl = stnode.getElementsByTagName("Value");
						for (int k=0;k<vnl.getLength();k++){
							Node namenode = vnl.item(k);
							Node namechild = namenode.getFirstChild();
							NamedNodeMap nnm = namenode.getAttributes();
							String name = nnm.item(0).getNodeValue();
							int value = Integer.parseInt(namechild.getNodeValue());
							System.out.println("name    : " + name+" "+value);
						}
						
					}
					
				} catch(ParserConfigurationException e) { 
					e.printStackTrace(); 
				} catch(Exception e) { 
					e.printStackTrace(); 
				}  
		
	}
	private void parseXML() {
		// TODO Auto-generated method stub
		String filePath = "output_apk";
		String fileName = "AndroidManifest.xml";
		
		File file = new File(filePath, fileName);
		if (!file.exists()){
			logger.error("AndroidManifest File not Exists!!!");
			return;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			Element el = doc.getDocumentElement();
			NodeList nl = el.getElementsByTagName("activity"); // Activity Element
			for (int i=0; i<nl.getLength(); i++)
			{
				Node node = nl.item(i);
				NamedNodeMap nnm = node.getAttributes();
				
				for (int j=0; j<nnm.getLength(); j++)
				{
					Node childNode = nnm.item(j);
					if (childNode.getNodeName().equals("android:name"))
						result.add(childNode.getTextContent());
				}
				
				logger.info("Activity Name - " + result.get(i));
			}
			
			nl = el.getElementsByTagName("manifest"); // manifest Element
//			ode node = nl.item(0);
			NamedNodeMap nnm = el.getAttributes();
				
			for (int j=0; j<nnm.getLength(); j++)
			{
				Node childNode = nnm.item(j);
				if (childNode.getNodeName().equals("package"))
					packageName = childNode.getTextContent();
			}
			
			logger.info("Package Name - " + packageName);
			
			APKDAL ad = new APKDAL();
			apk_id = ad.insertAPK(packageName, header.getVendor());
			
		} catch(ParserConfigurationException e) { 
			e.printStackTrace(); 
		} catch(Exception e) { 
			e.printStackTrace(); 
		}  
	}

	private void methodsummary() {
		// TODO Auto-generated method stub
		int static_field_size = 0;
		int instance_field_size =0;
		int direct_method_size = 0;
		int virtual_method_size = 0;
		String access_flag = "";
		String name = "";
		int class_id = 0;
		
		int[] cdsClassIdx = cds.getClass_idx();
		int[] cdsAF = cds.getAccess_flags();
		int[] cdsSuperIdx = cds.getSuperclass_idx();
		int[] cdsIntfOff = cds.getInterfaces_off();
		int[] cdsSourIdx = cds.getSource_file_idx();
		int[] cdsAnnOff = cds.getAnnotations_off();
		int[] cdsCDOff = cds.getClass_data_off();
		int[] cdsStatVOff = cds.getStatic_values_off();
		
		String log = "";
		
		for (int i=0;i<cds.getSize();i++){
			static_field_size = 0;
			instance_field_size =0;
			direct_method_size = 0;
			virtual_method_size = 0;
			access_flag = "";
			name = "";
			class_id = 0;
			
			log = separate+"\n";
			
			log +=   "\t[Class Description] : ";
			if (cdsClassIdx[i] >= 0){
				if (tis.getDescriptor_idx(cdsClassIdx[i]) >= 0){
					log += sdi.getDataS(tis.getDescriptor_idx(cdsClassIdx[i]));
					name = sdi.getDataS(tis.getDescriptor_idx(cdsClassIdx[i]));
				} else {
					log += "No Desc";
					name = "No_Desc";
				}
			} else {
				log += "No Desc";
				name = "No_Desc";
			}
			
			int libsize = ClassConstants.lib_list.length;
			boolean out = false;
			for (int ls=0;ls<libsize;ls++){
				if (name.startsWith(ClassConstants.lib_list[ls], 1)){
					out = true;
					break;
				}
			}
			if (out) {
				continue;
			}
			log += "\n\t[Access Flag      ] : ";
			log += Utils.accessFlag(cdsAF[i]);
			access_flag = Utils.accessFlag(cdsAF[i]);
			
			log += "\n\t[Superclass       ] : ";
			if (cdsSuperIdx[i] >= 0){
				if (tis.getDescriptor_idx(cdsSuperIdx[i]) >= 0){
					log += sdi.getDataS(tis.getDescriptor_idx(cdsSuperIdx[i]));
				} else {
					log += "No SuperClass";
				}
			} else {
				log += "No SuperClass";
			}
			
			log += "\n\t[Interface        ] : ";
			if (cdsIntfOff[i] > 0){
				int idx = typeList.getOffsetIdx(cdsIntfOff[i]);
				int size = typeList.getSize(idx);
				TypeItem ti = typeList.getList(idx);
				for (int j=0;j<size;j++){
					if (ti.getType_idx(j) >= 0){
						if (tis.getDescriptor_idx(ti.getType_idx(j)) >= 0){
							log += sdi.getDataS(tis.getDescriptor_idx(ti.getType_idx(j))) + " ";
						} else {
							log += "No Interface ";
						}
					} else {
						log += "No Interface ";
					}
				}
			} else {
				log += "No Interface";
			}
			
			log += "\n\t[Source           ] : ";
			if (cdsSourIdx[i] >= 0){
				int off = sis.getString_data_off(cdsSourIdx[i]);
				if (off > 0){
					log += sdi.getOffsetStr(off);
				} else {
					log += "No Source";
				}
			} else {
				log += "No Source";
			}
			
			log += "\n\t[Annotation       ] : ";
			if (cdsAnnOff[i] > 0){
				for (int j=0;j<adi.length;j++){
					if (adi[j].getOffset() == cdsAnnOff[i]){
						if (adi[j].getClass_annotations_off()>0){
							if (asi.getOffsetIdx(adi[j].getClass_annotations_off())>=0){
								AnnotationOff ao = asi.getAo(asi.getOffsetIdx(adi[j].getClass_annotations_off()));
								int[] off = ao.getAnnotation_off();
								for (int k=0;k<off.length;k++){
									for (int l=0;l<ai.length;l++){
										if (ai[l].getOffset() == off[k]){
											log += " visibility:"+ai[l].getVisibility();
											log += " Annotation:"+ai[l].getAnnotation().toString();
											break;
										}
									}
								}
							} else {
								log += "No Annotation";
							}
						} else {
							log += "No Annotation";
						}
						break;
					}
				}
			} else {
				log += "No Annotation";
			}
			
			log += "\n\t[ClassData        ] : ";
			if (cdsCDOff[i] > 0){
				int idx = cdi.getOffsetIdx(cdsCDOff[i]);
				if (idx>=0){
					log += "InstanceFieldSize:"+cdi.getInstance_fields_size(idx);
					log += " StaticFieldSize:"+cdi.getStatic_fields_size(idx);
					log += " DirectMethodSize:"+cdi.getDirect_methods_size(idx);
					log += " VirtualMethodSize:"+cdi.getVirtual_methods_size(idx);
					
					static_field_size = cdi.getStatic_fields_size(idx);
					instance_field_size = cdi.getInstance_fields_size(idx);
					direct_method_size = cdi.getDirect_methods_size(idx);
					virtual_method_size = cdi.getVirtual_methods_size(idx);
					
					ClassDAL cd = new ClassDAL();
					class_id = cd.insertClass(apk_id, static_field_size, instance_field_size, direct_method_size, virtual_method_size, access_flag, name);
					if (class_id == -1){
						log += "Errors\n";
					}
					
					EncodedField[] ifa = cdi.getInstance_field(idx);
					EncodedField[] sfa = cdi.getStatic_field(idx);
					EncodedMethod[] dma = cdi.getDirect_methods(idx);
					EncodedMethod[] vma = cdi.getVirtual_methods(idx);
					log += "\n\t\tInstance Field :"+fieldsAn(ifa, class_id);
					log += "\n\t\tStatic Field   :"+fieldsAn(sfa, class_id);
					log += "\n\t\tDirect Method  :"+methodsAn(dma, class_id);
					log += "\n\t\tVirtual Method :"+methodsAn(vma, class_id);
					
				} else {
					log += "No ClassData";
				}
			} else {
				log += "No ClassData";
			}
			
			log += "\n\t[StaticValue      ] : ";
			if (cdsStatVOff[i] > 0){
				for (int j=0;j<ear.length;j++){
					if (cdsStatVOff[i]==ear[j].getOffset()){
						log += ear[j].toString();
						break;
					}
				}
			} else {
				log += "No StaticValue";
			}
			
			logger.info(log);
			log = "";
		}
	}

	private void classsummary() {
		// TODO Auto-generated method stub
		int[] sidx = cds.getSource_file_idx();
		int[] cidx = cds.getClass_idx();
		int[] cdiIndex = cdi.getOffset();
		
		String re = "";
		
		logger.info("Classes summary!");
		logger.info("<Format> : Filename\tClassname\nStatic_field_size\tInstance_field_size\nDirect_method_size\tVirtual_method_size\nAccessFlag");
		logger.info("<Format> : And Details");
		
		for(int i=0;i<sidx.length;i++){
			logger.info(separate);
			int className = sidx[i];
			if (className==-1) {
				re = "No_Filename\t";
			} else {
				re = sdi.getDataS(className)+"\t";
			}
			
			int classIdx = cidx[i];
			int classStringIdx = tis.getDescriptor_idx(classIdx);
			String cName = "";
			if (classStringIdx==-1) {
				re += "No_Classname\t";
			} else {
				cName = sdi.getDataS(classStringIdx);
				re += cName +"\t";
			}
			
			int codeOffset = cds.getClass_data_off(i);
			int codeIdx = 0;
			if (codeOffset != -1){
				codeIdx= findByOffset(cdiIndex, codeOffset);
				String af = Utils.accessFlag(cds.getAccess_flags(i));
				if (codeIdx != -1){
					re += cdi.getStatic_fields_size(codeIdx)+"\t"+
							cdi.getInstance_fields_size(codeIdx)+"\t"+
							cdi.getDirect_methods_size(codeIdx)+"\t"+
							cdi.getVirtual_methods_size(codeIdx)+"\t";
					
					logger.info(re + af);
					re = ""; af = "";
					
					EncodedField[] sttField = cdi.getStatic_field(codeIdx);
					int fidiff = 0;
					for (int j=0;j<sttField.length;j++){
						if (j==0){
							logger.debug("--Static Field <Name> <Description> <AccessFlag>--");
						}
						fidiff += sttField[j].getField_idx_diff();
						re += sdi.getDataS(fi.getName_idx(fidiff))+"\t";
						re += sdi.getDataS(tis.getDescriptor_idx(fi.getType_idx(fidiff)))+"\t";
						af = Utils.accessFlag(sttField[j].getAccess_flags());
						logger.debug(re+af);
						re = ""; af = "";
					}
					
					
					EncodedField[] insField = cdi.getInstance_field(codeIdx);
					fidiff = 0;
					for (int j=0;j<insField.length;j++){
						if (j==0){
							logger.debug("--Instance Field <Name> <Description> <AccessFlag>--");
						}
						fidiff += insField[j].getField_idx_diff();
						re += sdi.getDataS(fi.getName_idx(fidiff))+"\t";
						re += sdi.getDataS(tis.getDescriptor_idx(fi.getType_idx(fidiff)))+"\t";
						af = Utils.accessFlag(insField[j].getAccess_flags());
						logger.debug(re+af);
						re = ""; af = "";
					}
					
					
					EncodedMethod[] dirMethod = cdi.getDirect_methods(codeIdx);
					int midiff = 0;
					for (int j=0;j<dirMethod.length;j++){
						if (j==0){
							logger.debug("--Direct Method <Name> <Description> <AccessFlag> <Opcode>--");
						}
						midiff += dirMethod[j].getMethod_idx_diff();
						re += sdi.getDataS(mi.getName_idx(midiff)).trim()+"()\t";
						re += sdi.getDataS(tis.getDescriptor_idx(mi.getClass_idx(midiff)))+"\t";
						af = Utils.accessFlag(dirMethod[j].getAccess_flags());
						logger.debug(re+af);
						re = ""; af = "";
						
						int dirCodeOff = dirMethod[j].getCode_off();
						int[] insns = ci.getInsnsByOff(dirCodeOff);
						for (int k=0;k<insns.length;k++){
							re += "["+insns[k]+"]\t";
							String dd = Utils.form(insns[k]);
							re += "["+dd+"]\n";
						}
						if (!re.equals("")){
							logger.debug("--Opcode of this method--");
							logger.debug(re);
						}
						re = "";
					}
					
					
					EncodedMethod[] virMethod = cdi.getVirtual_methods(codeIdx);
					midiff = 0;
					for (int j=0;j<virMethod.length;j++){
						if (j==0){
							logger.debug("--Virtual Method <Name> <Description> <AccessFlag> <Opcode>--");
						}
						midiff += virMethod[j].getMethod_idx_diff();
						re += sdi.getDataS(mi.getName_idx(midiff)).trim()+"()\t";
						re += sdi.getDataS(tis.getDescriptor_idx(mi.getClass_idx(midiff)))+"\t";
						af = Utils.accessFlag(virMethod[j].getAccess_flags());
						logger.debug(re+af);
						re = ""; af = "";
						
						int dirCodeOff = virMethod[j].getCode_off();
						int[] insns = ci.getInsnsByOff(dirCodeOff);
						for (int k=0;k<insns.length;k++){
							re += "["+insns[k]+"]\t";
							String dd = Utils.form(insns[k]);
							re += "["+dd+"]\n";
						}
						if (!re.equals("")){
							logger.debug("--Opcode of this method--");
							logger.debug(re);
						}
						re = "";
					}
				}
			}			
		}
		logger.info("Classes summary END!");
	}

	private void fieldsummary() {
		// TODO Auto-generated method stub
		
	}
	
	private int findByOffset(int[] obj, int find){
		Object result = null;
		for (int i=0;i<obj.length;i++){
			if (obj[i] == find)
				return i;
		}
		return -1;
	}
	
	private String fieldsAn(EncodedField[] efa, int cls_id){
		String result = "\n";
		int fidiff = 0;

		for (int i = 0;i<efa.length;i++){
			fidiff += efa[i].getField_idx_diff();
			result += "\t\t\tName:"+sdi.getDataS(fi.getName_idx(fidiff))+"\t";
			result += "Desc:"+sdi.getDataS(tis.getDescriptor_idx(fi.getType_idx(fidiff)))+"\t";
			result += "AF:"+Utils.accessFlag(efa[i].getAccess_flags())+"\n";
			
			String type = sdi.getDataS(tis.getDescriptor_idx(fi.getType_idx(fidiff)));
			
			FieldDAL fd = new FieldDAL();
			int field_id = fd.insertField(cls_id, apk_id, type, true);
			if (field_id == -1){
				result += "Errors\n";
			}
		}
		return result;
	}
	private String methodsAn(EncodedMethod[] ema, int cls_id){
		String name = "";
		String access_flag = "";
		String ret_type = "";
		int text_length = 0;
		int met_id = 0;
		String type = "";
		
		String result = "\n";
		int midiff = 0;
		for (int j=0;j<ema.length;j++){
			midiff += ema[j].getMethod_idx_diff();
			result += "\t\t\tName:"+sdi.getDataS(mi.getName_idx(midiff)).trim()+"()\t";
			name = sdi.getDataS(mi.getName_idx(midiff));
			result += "Desc:"+sdi.getDataS(tis.getDescriptor_idx(mi.getClass_idx(midiff)))+"\n\t\t\t\t";
			if (mi.getProto_idx(midiff) >= 0){
				if (pi.getShorty_idx(mi.getProto_idx(midiff)) >= 0)
					result += "ProtoDesc:"+sdi.getDataS(pi.getShorty_idx(mi.getProto_idx(midiff)))+"\t";
				
				if (pi.getReturn_type_idx(mi.getProto_idx(midiff)) >= 0){
					if (tis.getDescriptor_idx(pi.getReturn_type_idx(mi.getProto_idx(midiff))) >= 0) {
						result += "ProtoReturn:"+sdi.getDataS(tis.getDescriptor_idx(pi.getReturn_type_idx(mi.getProto_idx(midiff))))+"\t";
						ret_type = sdi.getDataS(tis.getDescriptor_idx(pi.getReturn_type_idx(mi.getProto_idx(midiff))));
					}
				}
				
				if (pi.getParameters_off(mi.getProto_idx(midiff)) > 0){
					int idx = typeList.getOffsetIdx(pi.getParameters_off(mi.getProto_idx(midiff)));
					if (idx >= 0){
						result += "ProtoParameterTypeSize:"+typeList.getSize(idx)+"\t";
						
						TypeItem ti = typeList.getList(idx);
						for (int i=0;i<ti.getType_idx().length;i++){
							if (tis.getDescriptor_idx(ti.getType_idx(i)) >= 0){
								result += "ProtoParameterTypeList("+(i+1)+"):"+sdi.getDataS(tis.getDescriptor_idx(ti.getType_idx(i)))+"\t";
								type += sdi.getDataS(tis.getDescriptor_idx(ti.getType_idx(i)));
							}
						}
					}
				}
			}
			
			result += "AF:"+Utils.accessFlag(ema[j].getAccess_flags())+"\n\t\t\t\t";
			access_flag = Utils.accessFlag(ema[j].getAccess_flags());
			
			int dirCodeOff = ema[j].getCode_off();
			int idx = ci.getIdxByOff(dirCodeOff);
			int[] insns = ci.getInsnsByOff(dirCodeOff);
			if (idx >= 0){
				int registerSize = ci.getRegisters_size(idx);
				int insSize = ci.getIns_size(idx);
				int OutSize = ci.getOuts_size(idx);
				int TrySize = ci.getTries_size(idx);
				int diOff = ci.getDebug_info_off(idx);
				TryItem[] ti = ci.getTries(idx);
				EncodedCatchHandlerList echl = ci.getEchl(idx);
				
				result += "RegSize:"+registerSize+"\tInsSize:"+insSize+"\tOutSize:"+OutSize+"\tTrySize:"+TrySize+"\n";
				if (ti != null){
					for (int k=0;k<ti.length;k++){
						result += "\t\t\t\t\tTry: StartAddr:"+ti[k].getStart_addr()+"\tInsCnt:"+ti[k].getInsn_count()+"\tHandlerOff:"+ti[k].getHandler_off()+"\n";
					}
				}
				if (dii != null){
					int diIdx = -1;
					for (int k=0;k<dii.length;k++){
						if (diOff == dii[k].getOffSet()){
							diIdx = k;
							break;
						}
					}
					
					if (diIdx>=0){
						ByteCode[] bc = dii[diIdx].getBc();
						int ls = dii[diIdx].getLineStart();
						int ps = dii[diIdx].getParametersSize();
						int[] n = dii[diIdx].getParameterName();
						result += "\t\t\t\t\tDebug: LineStart:"+ls+"\tParameterSize:"+ps;
						for (int k=0;k<n.length;k++){
							result += "\t["+sdi.getDataS(n[k])+"]";
						}
					
						result += "\n";
						String di = "";
						for (int k=0;k<bc.length;k++){
							di += "["+Utils.form(bc[k].getOpCodes())+"]";
							int[] arg = bc[k].getArguments();
							if (arg != null){
								for (int d=0;d<arg.length;d++){
									di += "\targ["+arg[d]+"]";
								}
								di += "\n\t\t\t\t\t\t";
							}
						}
						
						if (di!=""){
							result += "\t\t\t\t\t\tDebugOpcode:"+di+"\n";
						} else {
							result += "\n";
						}
					}
				}
				
				if (echl != null){
					EncodedCatchHandler[] ech = echl.getEch();
					for (int k=0;k<ech.length;k++){
						
					}
					int sz = echl.getSize();
				}
			}
			String dd = "";
			String ddesc = "";
			String ddesc2 = "";
			for (int k=0;k<insns.length;k++){
				dd += "["+Utils.form(insns[k])+"]";
				String opc = Utils.last2byte(insns[k]);
				int op = Integer.parseInt(opc,16);
				ddesc += "["+Utils.getOPDesc(op)+"]";
				ddesc2 += "["+Utils.getOPClass(op)+"]";
				text_length++;
				if (k!=insns.length-1){
					dd += "->";
					ddesc += "->";
					ddesc2 += "->";
				}
			}
			String d = "";
			if (insns.length>0){
				//d = analyse_insns(insns);
			}
			
			result += d;
			result += "\t\t\t\tOpcode:"+dd+"\n";
			
			if (dd!=""){
//				result += "\t\t\t\tOpcode:"+dd+"\n";
//				result += "\t\t\t\tOpdesc:"+ddesc+"\n";
//				result += "\t\t\t\tOpClsf:"+ddesc2+"\n";
			} else {
				result += "\n";
			}
			
			MethodDAL md = new MethodDAL();
			met_id = md.insertMethod(cls_id, apk_id, name, access_flag, ret_type, text_length);
			if (met_id == -1){
				result += "Errors\n";
			}
			int param_id = md.insertMethodParameters(met_id, type);
			if (param_id == -1){
				result += "Errors\n";
			}
			name = "";
			access_flag = "";
			ret_type = "";
			text_length = 0;
			met_id = 0;
			type = "";
			
		}
		return result;
	}
	
	private String analyse_insns(int[] insns){
		String result = "";

		String ddesc = "";
		String ddesc2 = "";
		
		for (int k=0;k<insns.length;k++){
			int ins = insns[k];
			int arg1 = -1;
			int arg2 = -1;
			int arg3 = -1;
			int arg4 = -1;
			String opc = "";
			int opi = -1;
			try{
			opc = Utils.last2byte(insns[k]);
			opi = Integer.parseInt(opc,16);
			arg1 = Integer.parseInt(Utils.first2byte(insns[k]));
			
			ddesc += "["+Utils.getOPDesc(opi)+"]";
			ddesc2 += "["+Utils.getOPClass(opi)+"]";
			} catch (Exception ex){
				ex.printStackTrace();
			}
			try{
			switch (opi){
			case 0x02:
			case 0x05:
			case 0x08:
			case 0x13:
			case 0x15:
			case 0x16:
			case 0x19:
			case 0x1A:
			case 0x1C:
			case 0x1F:
			case 0x20:
			case 0x22:
			case 0x23:
			case 0x29:
			case 0x2D:
			case 0x2E:
			case 0x2F:
			case 0x30:
			case 0x31:
			case 0x32:
			case 0x33:
			case 0x34:
			case 0x35:
			case 0x36:
			case 0x37:
			case 0x38:
			case 0x39:
			case 0x3A:
			case 0x3B:
			case 0x3C:
			case 0x3D:
			case 0x44:
			case 0x45:
			case 0x46:
			case 0x47:
			case 0x48:
			case 0x49:
			case 0x4A:
			case 0x4B:
			case 0x4C:
			case 0x4D:
			case 0x4E:
			case 0x4F:
			case 0x50:
			case 0x51:
			case 0x52:
			case 0x53:
			case 0x55:
			case 0x56:
			case 0x57:
			case 0x58:
			case 0x59:
			case 0x5A:
			case 0x5B:
			case 0x5C:
			case 0x5D:
			case 0x5E:
			case 0x5F:
			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
			case 0x68:
			case 0x69:
			case 0x6A:
			case 0x6B:
			case 0x6C:
			case 0x6D:
			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9A:
			case 0x9B:
			case 0x9C:
			case 0x9D:
			case 0x9E:
			case 0x9F:
			case 0xA0:
			case 0xA1:
			case 0xA2:
			case 0xA3:
			case 0xA4:
			case 0xA5:
			case 0xA6:
			case 0xA7:
			case 0xA8:
			case 0xA9:
			case 0xAA:
			case 0xAB:
			case 0xAC:
			case 0xAD:
			case 0xAE:
			case 0xAF:
			case 0xD0:
			case 0xD1:
			case 0xD2:
			case 0xD3:
			case 0xD4:
			case 0xD5:
			case 0xD6:
			case 0xD7:
			case 0xD8:
			case 0xD9:
			case 0xDA:
			case 0xDB:
			case 0xDC:
			case 0xDD:
			case 0xDE:
			case 0xDF:
			case 0xE0:
			case 0xE1:
			case 0xE2:
			case 0xF2:
			case 0xF3:
			case 0xF4:
			case 0xF5:
			case 0xF6:
			case 0xF7:
				arg2 = insns[++k];
				break;
				
			case 0x14:
			case 0x17:
			case 0x24:
			case 0x25:
			case 0x26:
			case 0x2B:
			case 0x2C:
			case 0x6E:
			case 0x6F:
			case 0x70:
			case 0x71:
			case 0x72:
			case 0x74:
			case 0x75:
			case 0x76:
			case 0x77:
			case 0x78:
			case 0xEE:
			case 0xF0:
			case 0xF8:
			case 0xF9:
			case 0xFA:
			case 0xFB:
				arg2 = insns[++k];
				arg3 = insns[++k];
				break;
				
			case 0x18:
				arg2 = insns[++k];
				arg3 = insns[++k];
				arg4 = insns[++k];
				break;
				
			default:
				break;
			}
			if (k!=insns.length-1){
				ddesc += "->";
				ddesc2 += "->";
			}
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
		result = "\t\t\t\t\tOpCode"+ddesc+"\n\t\t\t\t\tOpClass"+ddesc2+"\n";
		return result;
	}
}

