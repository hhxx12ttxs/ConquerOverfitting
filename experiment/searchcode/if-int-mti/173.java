package net.smstpdu.tpdu;

import java.util.ArrayList;
import java.util.Arrays;

import net.smstpdu.GSMAlphabet;
import net.smstpdu.MessageType;
import net.smstpdu.NumberingPlan;
import net.smstpdu.TBCD;
import net.smstpdu.TPDA_OA_DA;
import net.smstpdu.TPValidityPeriodFormat;
import net.smstpdu.TypeOfNumber;
import net.smstpdu.ValidityPeriod;
import net.smstpdu.tpdcs.TPDCS;
import net.smstpdu.tpdu.udh.UDH;
import net.smstpdu.tppid.TPPID;
import net.smstpdu.tppid.TPPID_Interworking;
import net.utils.Utils;

public class SmsSubmit extends TPDU {
	public boolean TP_RD;
	public TPValidityPeriodFormat TP_VPF;
	public boolean TP_SRR;
	public boolean TP_UDHI;
	public boolean TP_RP;
	public int TP_MR;
	public TPDA_OA_DA TP_DA;
	public TPPID TP_PID;
	public TPDCS TP_DCS;
	public int TP_VP;
	public int TP_UDL;
	public byte[] TP_UD;
	public UDH udh;
	
	public byte[] encode(){
		ArrayList<Byte> buff = new ArrayList<Byte>();
		byte tmp_byte = 0;
		byte[] tmp_buff = null;

		// TP-RD
		tmp_byte += (TP_RD ? (byte)0x04 : 0);
		// TP-VPF
		tmp_byte += TP_VPF.getId();
		// TP-SRR
		tmp_byte += (TP_SRR ? (byte)0x20 : 0);
		// TP-UDHI
		tmp_byte += (TP_UDHI ? (byte)0x40 : 0);
		// TP-RP
		tmp_byte += (TP_RP ? (byte)0x80 : 0);
		// TP-MTI
		tmp_byte += 0x01;
		buff.add(tmp_byte);

		// TP-MR
		buff.add((byte)TP_MR);
		// TP_DA
		buff.add((byte)TBCD.decode(TP_DA.digits).length());
		tmp_byte = (byte)0x80; // no extension
		tmp_byte += TP_DA.typeOfNumber.getId() + TP_DA.numberingPlan.getId();
		buff.add(tmp_byte);
		Utils.bytesToLst(buff, TP_DA.digits);
		// TP-PID
		tmp_byte = (byte)TP_PID.format.getId();
		switch(TP_PID.format){
			case DEFAULT1:
				tmp_byte += TP_PID.interworking.getId();
				if(TP_PID.interworking == TPPID_Interworking.TELEMATIC_INTERWORKING) tmp_byte += TP_PID.device_type1.getId();
				break;
			case DEFAULT2:
				tmp_byte += TP_PID.device_type2.getId();
				break;
		}
		buff.add(tmp_byte);

		// TP-DCS
		tmp_byte = TP_DCS.encode();
		buff.add(tmp_byte);
		
		// TP-VP
		switch(TP_VPF){
			case TP_VP_Relative:
				buff.add((byte)TP_VP);
				break;
			case TP_VP_Enhanced:
				Utils.bytesToLst(buff, new byte[7]);
				break;
			case TP_VP_Absolute:
				Utils.bytesToLst(buff, new byte[7]);
				break;
		
		}

		// TP-UDL
		if(TP_UDHI){
			// encode udh
			tmp_buff = udh.encode();
			// calculate TP_UDL
			switch(TP_DCS.encoding){
				case DEFAULT:
					TP_UDL = (int)((tmp_buff.length * 8 + GSMAlphabet.getPrePaddingBits(tmp_buff.length)) / 7) + TP_UDL;
					break;
				case _8BIT:
					TP_UDL = TP_UD.length + udh.length + 1;
					break;
				case UCS2:
					TP_UDL = TP_UD.length + udh.length + 1;
					break;
					
			}
		}else{
			switch(TP_DCS.encoding){
				case DEFAULT:
					//TP_UDL = (int)Math.ceil((double)((TP_UD.length) * 8) / 7);
					//TP_UDL = GSMAlphabet.decode(TP_UD).length();
					// has to be calculated before
					break;
				case _8BIT:
					TP_UDL = TP_UD.length;
					break;
				case UCS2:
					TP_UDL = TP_UD.length;
					break;
			}
		}
		buff.add((byte)TP_UDL);
		
		// TP-UDH
		if(TP_UDHI && tmp_buff != null) Utils.bytesToLst(buff, tmp_buff);
		
		// TP-UD
		Utils.bytesToLst(buff, TP_UD);
		// result
		return Utils.list2array(buff);
	}

	public void init(byte[] data){
		// TP-RD
		TP_RD =  (data[byte_pos] & 0x04) == 0x04;
		// TP-VPF
		TP_VPF = TPValidityPeriodFormat.get(data[byte_pos] & 0x18);
		// TP-SRR
		TP_SRR = (data[byte_pos] & 0x20) == 0x20;
		// TP-UDHI
		TP_UDHI = (data[byte_pos] & 0x40) == 0x40;
		// TP-RP
		TP_RP = (data[byte_pos] & 0x80) == 0x80;
		byte_pos++;

		// TP-MR
		TP_MR = data[byte_pos] & 0xFF;
		byte_pos++;
		
		// TP_DA
		TP_DA = new TPDA_OA_DA();
		TP_DA.length = data[byte_pos] & 0xFF;
		byte_pos++;
		TP_DA.typeOfNumber = TypeOfNumber.get(data[byte_pos] & 0x70);
		TP_DA.numberingPlan = NumberingPlan.get(data[byte_pos] & 0x0F);
		byte_pos++;
		// BCD encoded digits, 2 digits per byte
		if(TP_DA.length > 0){
			TP_DA.digits = Arrays.copyOfRange(data, byte_pos, byte_pos + (int)Math.ceil((double)(TP_DA.length) / 2));
			byte_pos += TP_DA.digits.length;
		}

		// TP-PID
		TP_PID = new TPPID();
		TP_PID.init(data[byte_pos]);
		byte_pos++;
		
		// TP-DCS
		TP_DCS = TPDCS.decode(data[byte_pos]);
		byte_pos++;
		// TP-VP
		switch(TP_VPF){
			case TP_VP_Relative:
				TP_VP = ValidityPeriod.decode(data[byte_pos] & 0xFF);
				byte_pos++;
				break;
			case TP_VP_Enhanced:
				byte_pos += 7;
				break;
			case TP_VP_Absolute:
				byte_pos += 7;
				break;
		
		}
		// if DCS found and data exists
		if(TP_DCS != null && byte_pos < data.length){
			// TP-UDL
			TP_UDL = data[byte_pos] & 0xFF;
			//System.out.println("UDL: " + String.format("%02x", data[byte_pos]));
			byte_pos++;

			if(TP_UDL > 0){
				// User Data header
				if(TP_UDHI){
					// security check, some messages have UDHI set but actual
					// UDH is missing
					if((data[byte_pos] & 0xFF) < data.length - byte_pos){
						udh = new UDH();
						udh.length = data[byte_pos] & 0xFF;
						//System.out.println("UDHL: " + udh.length);
						byte_pos++;
						udh.init(Arrays.copyOfRange(data, byte_pos, byte_pos + udh.length));
						byte_pos += udh.length;
					}
				}
				// TP-UD
				TP_UD = Arrays.copyOfRange(data, byte_pos, data.length);
				
			}
			
		}
		
		

		
	}
	
	
	public SmsSubmit(){
		super();
		type = MessageType.SMS_SUBMIT;
	}

}

