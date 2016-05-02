package net.ber;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import net.asn1.compiler.ASNTag;
import net.asn1.compiler.ASNTagClass;
import net.asn1.compiler.ASNTagComplexity;
import net.asn1.compiler.ASNType;
import net.asn1.compiler.ElementDescriptor;
import net.utils.Utils;

public class BerTranscoderv2 {

	private byte[] data;
	public int byte_position = 0;
	public BerTag document;
	public boolean buildTree;
	
	
	private BerTag decodeTag(byte tag){
		BerTag res = new BerTag();
		int cl;
		int cmplx;
		
		// Tag class
		cl = tag & 0xC0;
		// PRIMITIVE/CONSTRUCTED
		cmplx = tag & 0x20;
		
		res.asn_class = ASNTagClass.get(cl);// fromId(cl);
		res.asn_pc = ASNTagComplexity.fromId(cmplx);
		// tag has multiple bytes
		if((tag & 0x1f) == 0x1f){
			// process octets
			boolean done = false;
			int tmp = 0;
			int c = 0;
			int old_byte_pos = byte_position;
			byte_position++;
			ArrayList<Integer> tmp_lst = new ArrayList<Integer>();
			while(!done && byte_position < data.length && byte_position > 0){
				tmp = data[byte_position] & 0x7f;
				tmp_lst.add(tmp);
				res.tag_length++;
				c++;
				done = (data[byte_position] & 0x80) == 0x00;
				byte_position++;
				
			}
			// reverse bits
			for(int i = 0; i<tmp_lst.size(); i++) res.tag |= tmp_lst.get(i) << (7 * (c - i - 1));
			byte_position = old_byte_pos;
			//System.out.println("decodeTagAAAAAAAAAAAAAAAA: " + res.tag);
			//System.out.println("decodeTagBBBBBBBBBBBBBBBB: " + res.tag_length);
			/*
			for(int i = 0; i<l; i++) {
				res.length += ((data[byte_position] & 0xFF )<< (8*(l-i-1)));
				byte_position++;
			}
			*/
			
		}else{
			res.tag = tag & 0x1F;
			res.tag_length = 1;
			
		}
		
		return res;
		
	}


	private LengthDescriptor decodeLength(){
		// exception handler only temporary
		LengthDescriptor res = new LengthDescriptor();
		try{
			byte tmp = data[byte_position];
			int l;
			// definite form, multiple octes
			if((tmp & 0xFF) > 0x80){
				res.lengthType = BerLengthType.DEFINITE_LONG;
				// number of octets for the length value
				l = data[byte_position] & 0x7F;
				//System.out.println("AAAAAAAAAAAAAAA: " + byte_position);
				res.lengthSize = l + 1;
				byte_position++;
				// process octets
				for(int i = 0; i<l; i++) {
					res.length += ((data[byte_position] & 0xFF )<< (8*(l-i-1)));
					byte_position++;
				}
				//System.out.println("LONG: " + res.length);
				//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!");
			// indefinite form
			}else if((tmp & 0xFF) == 0x80){
				res.lengthType = BerLengthType.INDEFINITE;
				res.lengthSize = 1;
				byte_position++;
				//System.out.println("NDEF: " + res.length);
				//System.out.println("INDEFINITE LENGTH");
			
			// definite form, single octet
			}else{
				res.lengthType = BerLengthType.DEFINITE_SHORT;
				res.lengthSize = 1;
				res.length = data[byte_position];
				byte_position++;
				//System.out.println("SHORT: " + res.length);
			}
			// length sanity check
			if(res.length > data.length || res.length < 0){
				throw new BERInvalidLength(res.length);
			}
		}catch(BERInvalidLength e){
			//System.out.println("INVALID BER LENGTH: " + res.length);
			
			res.length = 0;
			// only temporary
			/*
			e.printStackTrace();
			long ts = System.currentTimeMillis();
			File f = new File("/mnt/vazom_rt/" + ts + ".raw");
			try{
				FileOutputStream fo = new FileOutputStream(f);
				for(int i = 0; i<data.length; i++) fo.write(data[i]);
				fo.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
			*/
		}catch(Exception e){
			//e.printStackTrace();
			/*
			long ts = System.currentTimeMillis();
			//System.out.println("Error in PACKET["+ts+"]");
			//e.printStackTrace();
			File f = new File("/mnt/vazom_rt/" + ts + ".raw");
			try{
				FileOutputStream fo = new FileOutputStream(f);
				for(int i = 0; i<data.length; i++) fo.write(data[i]);
				fo.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
			*/		
		}
		return res;
		
	}
	
	private Object decodeCHOICE(ASNType start_obj, BerTag node){
		ASNType obj = null;
		BerTag btag;
		btag = decodeTag(data[byte_position]);
		byte_position += btag.tag_length;
		ArrayList<ElementDescriptor> choice_options;
		ElementDescriptor ed;
		ArrayList<BerTag> choice_selections = null;
		//System.out.println("CHOICE " + start_obj);
		try{
			choice_options = start_obj.elements;
			for(int i = 0; i<choice_options.size(); i++){
				ed = choice_options.get(i);

				if(ASNTag.fromId((byte)ed.tagValue) == ASNTag.CHOICE){
					ed.set();
					choice_selections = getCHOICESelections(ed.data, null);
					if(tagFound(btag, choice_selections)){
						obj = ed.data;
						obj.choiceSelection = obj;
						break;
					}
					
				}else{
					if(ed.tagValue == btag.tag && ed.tagClass == btag.asn_class){
						ed.set();
						obj = ed.data;
						obj.choiceSelection = obj;
						break;
					}
					
				}
				
			}
			if(obj != null){
				byte_position -= btag.tag_length;
				
				decodeType(obj, node);
				
				// update CHOICE class
				if(buildTree){
					node.node.tag = btag.tag;
					node.node.asn_class = btag.asn_class;
					node.node.asn_pc = btag.asn_pc;
					node.node.length = obj.length;
					node.node.berTag.lengthSize = obj.berTag.lengthSize;
					node.node.byte_position = obj.byte_position;
					node.hasData = false;
					
					// update CHOICE selection
					node.children.get(node.children.size() - 1).node.tag = btag.tag;
					node.children.get(node.children.size() - 1).node.asn_class = btag.asn_class;
					node.children.get(node.children.size() - 1).node.asn_pc = btag.asn_pc;
					
				}		
			// Unknown choice element, skip
			}else{
				node.hasData = false;
				LengthDescriptor ld = decodeLength();
				//System.out.println("UNKNOWN CHOICE, SKIPPING!");
				if(ld.lengthType == BerLengthType.INDEFINITE){
					byte_position = findEOC() + 2;
					
				}else byte_position += ld.length;
				
				
				
			}

			
			
			
		}catch(Exception e){
			//System.out.println("======= decodeCHOICE start ======");
			//System.out.println("byte_position: " + byte_position);
			//e.printStackTrace();
			//System.out.println("======= decodeCHOICE end ======");
		}		
		return start_obj;
	}
	

	private boolean tagFound(ElementDescriptor needle, BerTag haystack){
		return (needle.tagValue == haystack.tag && needle.tagClass == haystack.asn_class);
		
	}
	private boolean tagFound(BerTag needle, ArrayList<BerTag> haystack){
		BerTag bt;
		for(int i = 0; i<haystack.size(); i++){
			bt = haystack.get(i);
			if(bt.tag == needle.tag && bt.asn_class == needle.asn_class) return true;
			
		}
		return false;
		
	}

	public ArrayList<BerTag> getCHOICESelections(ASNType start_obj, ArrayList<BerTag> lst){
		ArrayList<BerTag> res;
		BerTag bt;
		ElementDescriptor ed;
		
		if(lst == null) res = new ArrayList<BerTag>(); else res = lst;
		for(int i = 0; i<start_obj.elements.size(); i++){
			ed = start_obj.elements.get(i);
			if(ed.tagValue == ASNTag.name2Tag(ASNTag.CHOICE.toString())){
				ed.set();
				res = getCHOICESelections(ed.data, res);
				ed.data = null;
			}else{
				bt = new BerTag();
				bt.tag = ed.tagValue;
				bt.asn_class = ed.tagClass;
				res.add(bt);
				
			}
			
		}
		return res;
		
	}

	private Object decodeSET(ASNType start_obj, BerTag node){
		BerTag btag;
		ASNType obj = null;
		int max_length;
		int old_pos;
		LengthDescriptor ld;
		boolean tag_found = false;
		ArrayList<ElementDescriptor> seq_elements;
		ArrayList<BerTag> choice_selections;
		ElementDescriptor ed = null;
		int extra_skip = 0;
		try{
			
			//System.out.println("SET: " + start_obj);
			btag = decodeTag(data[byte_position]);
			node.tag = btag.tag;
			node.asn_class = btag.asn_class;
			node.asn_pc = btag.asn_pc;
			
			start_obj.tag = node.tag;
			start_obj.asn_class = node.asn_class;
			start_obj.asn_pc = node.asn_pc;
			
			byte_position += btag.tag_length;
			
			ld = decodeLength();
			node.lengthSize = ld.lengthSize;

			if(ld.lengthType == BerLengthType.INDEFINITE){
				int tmp_pos = byte_position;
				int eoc_pos = findEOC();
				max_length = eoc_pos - tmp_pos;
				extra_skip = 2;
				
				
			}else{
				max_length = ld.length;
				
			}
			
			//System.out.println("SET LENGTH: " + ld.length);
			start_obj.length = max_length;
			start_obj.byte_position = byte_position;
			seq_elements = start_obj.elements;
			old_pos = byte_position;
			// process fields
			while(byte_position - old_pos < max_length & byte_position > 0){
				btag = decodeTag(data[byte_position]);
				for(int i = 0; i<seq_elements.size(); i++){
					ed = seq_elements.get(i);
					tag_found = tagFound(ed, btag);
					if(!tag_found){
						// if CHOICE tag, gather all available choice selections
						if(ed.tagValue == ASNTag.name2Tag(ASNTag.CHOICE.toString())){
							ed.set();
							choice_selections = getCHOICESelections(ed.data, null);
							// check if tag matches any of the available choice selections
							tag_found = tagFound(btag, choice_selections);
						// if ANY type, check if current position is within range,
						// if it is, mark this tag as match
						}else if(ed.tagValue == ASNTag.name2Tag(ASNTag.ANY.toString())){
							if(byte_position - old_pos < max_length) tag_found = true;
						}
					}
					if(tag_found) break;
					
				}
				if(ed != null && tag_found){
					ed.set();
					obj = ed.data;
					decodeType(obj, node);

					tag_found = false;
					ed = null;
				// unknown element
				}else{
					//System.out.println("!!!!");
					byte_position += btag.tag_length;
					ld = decodeLength();
					if(ld.lengthType == BerLengthType.INDEFINITE){
						byte_position = findEOC() + 2;
						
					}else byte_position += ld.length;
					
				}
			}
			byte_position += extra_skip;
	
		}catch(Exception e){
			//e.printStackTrace();
			//System.exit(0);
		}
		return start_obj;
		
	}	
	
	private Object decodeSEQUENCE(ASNType start_obj, BerTag node){
		BerTag btag;
		ASNType obj = null;
		int max_length;
		int old_pos;
		LengthDescriptor ld;
		boolean tag_found;
		ArrayList<ElementDescriptor> seq_elements;
		ArrayList<BerTag> choice_selections;
		ElementDescriptor ed;
		int extra_skip = 0;
		try{
			
			//System.out.println("SEQUENCE: " + start_obj);
			btag = decodeTag(data[byte_position]);
			node.tag = btag.tag;
			node.asn_class = btag.asn_class;
			node.asn_pc = btag.asn_pc;
			
			start_obj.tag = node.tag;
			start_obj.asn_class = node.asn_class;
			start_obj.asn_pc = node.asn_pc;
			
			byte_position += btag.tag_length;
			
			ld = decodeLength();
			node.lengthSize = ld.lengthSize;

			if(ld.lengthType == BerLengthType.INDEFINITE){
				int tmp_pos = byte_position;
				int eoc_pos = findEOC();
				max_length = eoc_pos - tmp_pos;
				extra_skip = 2;
				
				
			}else{
				max_length = ld.length;
				
			}
			
			//System.out.println("SEQUENCE LENGTH: " + ld.length);
			start_obj.length = max_length;
			start_obj.byte_position = byte_position;
			seq_elements = start_obj.elements;
			old_pos = byte_position;
			// process fields
			for(int i = 0; i<seq_elements.size(); i++) if(byte_position - old_pos < max_length){
				ed = seq_elements.get(i);
				btag = decodeTag(data[byte_position]);
				// mandatory field
				if(!ed.optional){
					ed.set();
					obj = ed.data;
					decodeType(obj, node);

				// optional fields
				}else{
					// check if tags match
					tag_found = tagFound(ed, btag);
					
					if(!tag_found){
						// if CHOICE tag, gather all available choice selections
						if(ed.tagValue == ASNTag.name2Tag(ASNTag.CHOICE.toString())){
							ed.set();
							choice_selections = getCHOICESelections(ed.data, null);
							// check if tag matches any of the available choice selections
							tag_found = tagFound(btag, choice_selections);
							//System.out.println(tag_found);
						// if ANY type, check if current position is within range,
						// if it is, mark this tag as match
						}else if(ed.tagValue == ASNTag.name2Tag(ASNTag.ANY.toString())){
							if(byte_position - old_pos < max_length) tag_found = true;
						}
					}
					// if tag match found, decode it
					if(tag_found){
						ed.set();
						obj = ed.data;
						decodeType(obj, node);
						
						//System.out.println(obj);
					}
					
				}
				tag_found = false;
				//System.out.println("TAG: " + btag.tag);
			}
			byte_position += extra_skip;
	
		}catch(Exception e){
			//e.printStackTrace();
			//System.exit(0);
		}
		return start_obj;
		
	}
	private void decodePRIMITIVE(ASNType start_obj, BerTag node){
		int l;
		byte[] value;
		LengthDescriptor ld;
		BerTag btag = decodeTag(data[byte_position]);
		node.tag = btag.tag;
		node.asn_class = btag.asn_class;
		node.asn_pc = btag.asn_pc;
		
		start_obj.tag = node.tag;
		start_obj.asn_class = node.asn_class;
		start_obj.asn_pc = node.asn_pc;
		
		byte_position += btag.tag_length;

		ld = decodeLength();
		node.lengthSize = ld.lengthSize;
		l = ld.length;
		try{
			start_obj.length = l;
			start_obj.byte_position = byte_position;
			
			value = new byte[l];
			for(int i = 0; i<l; i++) value[i] = start_obj.dataPacket[i + byte_position];
			start_obj.value = value;
			
			
			byte_position +=l;
		}catch(Exception e){
			//e.printStackTrace();
		}
		
		
	}
	private int findEOC(){
		int old_pos = byte_position;
		int res = 0;
		BerTag btag;
		LengthDescriptor ld;
		while(byte_position<data.length && byte_position > 0){
			btag = decodeTag(data[byte_position]);
			byte_position += btag.tag_length;
			ld = decodeLength();
			// primitive type, just skip the value part
			if(btag.asn_pc == ASNTagComplexity.Primitive){
				byte_position += ld.length;
				
			// consturcted type, if INDEFINITE look for EOC mark, else just skip the value part
			}else if(btag.asn_pc == ASNTagComplexity.Constructed){
				if(ld.lengthType == BerLengthType.INDEFINITE) byte_position += (findEOC() - byte_position) + 2;
				else byte_position += ld.length;
			}
			// check for EOC = two 0x00 bytes
			if(byte_position < data.length - 1) if(data[byte_position] == 0x00 && data[byte_position+1] == 0x00) break;
			
		}
		res = byte_position;
		byte_position = old_pos;
		return res;
	}
	
	private Object decodeANY(ASNType start_obj, BerTag node){
		BerTag btag;
		int l;
		byte[] value;
		int extra_skip = 0;
		LengthDescriptor ld;
		try{
			//System.out.println("decodeANY: " + start_obj);
			// Update tag info from decoded information
			btag = decodeTag(data[byte_position]);
			node.tag = btag.tag;
			node.asn_class = btag.asn_class;
			node.asn_pc = btag.asn_pc;
			
			start_obj.tag = node.tag;
			start_obj.asn_class = node.asn_class;
			start_obj.asn_pc = node.asn_pc;
			
			
			byte_position += btag.tag_length;
			ld = decodeLength();
			
			if(ld.lengthType == BerLengthType.INDEFINITE){
				int tmp_pos = byte_position;
				int eoc_pos = findEOC();
				l = eoc_pos - tmp_pos;
				extra_skip = 2;
				//System.out.println("NDEF: " + l);
				
			}else{
				l = ld.length;
				
			}
			
			//System.out.println("ANY LENGTH: " + l);
			node.lengthSize = ld.lengthSize;
			
			start_obj.length = l;
			start_obj.byte_position = byte_position;

			value = new byte[l];
			for(int i = 0; i<l; i++) value[i] = start_obj.dataPacket[i + byte_position];
			start_obj.value = value;
			byte_position += l + extra_skip;

		}catch(Exception e){
			//e.printStackTrace();
			/*
			long ts = System.currentTimeMillis();
			//System.out.println("[ANY] Error in PACKET["+ts+"]");
			//e.printStackTrace();
			File f = new File("/mnt/vazom_rt/" + ts + ".raw");
			try{
				FileOutputStream fo = new FileOutputStream(f);
				for(int i = 0; i<data.length; i++) fo.write(data[i]);
				fo.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
			*/			
		}
		return start_obj;
		
	}

	
	private Object decodeEXPLICIT(ASNType start_obj, BerTag node){
		int l;
		LengthDescriptor ld;
		BerTag btag;
		try{
			btag = decodeTag(data[byte_position]);
			node.tag = btag.tag;
			node.asn_class = btag.asn_class;
			node.asn_pc = btag.asn_pc;
			
			start_obj.tag = node.tag;
			start_obj.asn_class = node.asn_class;
			start_obj.asn_pc = node.asn_pc;

			byte_position += btag.tag_length;

			
			ld = decodeLength();
			node.lengthSize = ld.lengthSize;
			l = ld.length;

			start_obj.length = l;
			start_obj.byte_position = byte_position;

			start_obj.elements.get(0).set();
			
			// process field
			decodeType(start_obj.elements.get(0).data, node);

		}catch(Exception e){
			//e.printStackTrace();
		}
	
		return start_obj;
	}
	
	private Object decodeOF(ASNType start_obj, BerTag node){
		int l;
		int offset = 0;
		LengthDescriptor ld;
		int extra_skip = 0;
		BerTag btag;
		try{
			//System.out.println("SEQUENCE OF: " + start_obj);
			btag = decodeTag(data[byte_position]);
			node.tag = btag.tag;
			node.asn_class = btag.asn_class;
			node.asn_pc = btag.asn_pc;
			
			start_obj.tag = node.tag;
			start_obj.asn_class = node.asn_class;
			start_obj.asn_pc = node.asn_pc;
			
			byte_position += btag.tag_length;
			ld = decodeLength();
			
			if(ld.lengthType == BerLengthType.INDEFINITE){
				int tmp_pos = byte_position;
				int eoc_pos = findEOC();
				l = eoc_pos - tmp_pos;
				extra_skip = 2;
			}else{
				l = ld.length;
			}
			
			
			node.lengthSize = ld.lengthSize;
			start_obj.length = l;
			start_obj.byte_position = byte_position;
			
			offset = byte_position;
			
			while((byte_position - offset < l) && byte_position > 0){
				start_obj.addChild();
				decodeType(start_obj.of_children.get(start_obj.of_children.size() - 1), node);
				
			}
			byte_position += extra_skip;
			
		}catch(Exception e){
			//e.printStackTrace();
		}		
		return start_obj;
		
	}
	
	private ASNType decodeType(ASNType start_obj, BerTag node){
		ASNType res = null;
		ASNTagComplexity asn_pc;
		Integer asn_tag;
		BerTag btag = null;
		
		
		//System.out.println("----------------------");
		try{
			res = start_obj;
			
			// value points to original data stream
			res.dataPacket = data;
			
			
			if(buildTree){
				if(node.node == null){
					node.node = res;
					btag = node;
					res.berTag = btag;
				}else{
					btag = new BerTag();
					btag.node = res;
					btag.children = new ArrayList<BerTag>();
					node.children.add(btag);
					btag.parent = node;
					res.berTag = btag;
				}
				
			}
			// get ASN complexity tag 
			asn_pc = res.asn_pc;
			
			// get ASN universal tag 
			asn_tag = res.tag;
			// if Constructed, process children
			if(asn_pc == ASNTagComplexity.Constructed){
				// CHOICE = special case
				if(ASNTag.fromId(asn_tag.byteValue()) == ASNTag.CHOICE){
					decodeCHOICE(res, btag);
				// SEQUENCE, SEQUENCE OF, generic ASNType
				}else{
					// EXPLICIT ASNType
					if(res.explicit){
						decodeEXPLICIT(res, btag);
					// OF Type
					}else if(res.of_children != null){
						decodeOF(res, btag);
					// SET type
					}else if(res.universalTag == ASNTag.SET){
						decodeSET(res, btag);
					// SEQUENCE
					}else{
						decodeSEQUENCE(res, btag);
						
					}
				}
				
			// ANY type
			}else if(asn_pc == ASNTagComplexity.Unknown){
				//if(buildTree) leaf.add(btag);
				//System.out.println("ANY: " + btag.node);
				decodeANY(res, btag);
				
			// Primitive type
			}else{
				//if(buildTree) leaf.add(btag);
				//System.out.println("PRIMITIVE: " + btag.node);
				decodePRIMITIVE(res, btag);
				
			}
			//System.out.println("----------------------");
		}catch(Exception e){
			//e.printStackTrace();
		}
		
		
		
		return res;
		
	}
	public void showTree(BerTag btag, int depth){
		for(int i = 0; i<depth; i++) System.out.print("  ");
		if(btag.node != null) System.out.println(btag.node + ":" + btag.node.length + ":" + btag.hasData + ":" + btag.lengthSize);
		
		for(int i = 0; i<btag.children.size(); i++){
			for(int j = 0; j<depth; j++) System.out.print("  ");
			//System.out.println(btag.children.get(i).node);
			showTree(btag.children.get(i), depth+1);
			
		}
		
		
	}
	private byte[] encodeLength(BerTag tag){
		byte[] res = new byte[tag.lengthSize];
		////System.out.println("encodeLength: " + tag.node + ":" + res.length);
		if(tag.lengthType == BerLengthType.DEFINITE_SHORT){
			res[0] = (byte)tag.node.length;
		}else{
			res[0] = (byte)((tag.lengthSize - 1) | 0x80);
			for(int i = 0; i<tag.lengthSize - 1; i++){
				res[i + 1] = (byte)(tag.node.length >> (8*(tag.lengthSize-1 - (i + 1))));
				////System.out.println("encodeLength: " + res[i+1]);
			}
		}
		
		
		return res;
		
	}
	private BerTag calculateLength(int length){
		int l;
		int b;
		BerTag res = new BerTag();
		if(length > 127){
			// num of octets
			b = (int)Math.ceil(Math.log10(length + 1) / Math.log10(2));
			l = (int)Math.ceil((double)b / 8);
			//l = (int)Math.ceil((double)length / 255);
			res.lengthType = BerLengthType.DEFINITE_LONG;
			//  l = num of octets for actual length
			// +1 = num of octets occupied by length value
			res.lengthSize = l + 1;
			
			
		}else{
			res.lengthType = BerLengthType.DEFINITE_SHORT;
			res.lengthSize = 1;
			//res.node.length = length;
		}
		return res;
		
	}


	
	public void clearLengths(BerTag root){
		if(root.node != null){
			if(root.node.value != null) root.node.length = 0;
		}
		for(int i = 0; i<root.children.size(); i++){
			clearLengths(root.children.get(i));
		}
		
	}

	
	public int getChildrenLength(BerTag root){
		int res = 0;
		int cl;
		BerTag tmp;
		BerTag tag;

		for(int i = 0; i<root.children.size(); i++){
			tag = root.children.get(i);

			if(tag.hasData){
				if(tag.node.value != null){
					tmp = calculateLength(tag.node.value.length);
					tag.node.length = tag.node.value.length;
					// Length of L(length) + Length of V(Value) + Tag(1 octet)
					res += tmp.lengthSize + tag.node.length + 1;
					//System.out.println("getChildrenLength: " + tag.node + ":" + (tag.node.length + 1 + tmp.lengthSize));
				}else{
					// SEQUENCE type, only TAG and LENGTH
					cl = getChildrenLength(tag);
					tmp = calculateLength(cl);
					// Length of L(length) + Tag(1 octet)
					res += tmp.lengthSize + 1;
					//System.out.println("getChildrenLength: " + tag.node + ":" + 2);
				}
			}
			res += getChildrenLength(tag);
		}
		return res;
	}

	
	public void prepareLenghts(BerTag root){
		BerTag tmp;
		int l = 0;
		if(root.hasData){
			if(root.children.size() == 0){
				if(root.node.value != null){
					root.node.length = root.node.value.length;
					tmp = calculateLength(root.node.length);
					root.lengthSize = tmp.lengthSize;
					root.lengthType = tmp.lengthType;
				// NULL value or zero length value
				}else{
					root.node.length = 0;
					root.lengthSize = 1;
					root.lengthType = BerLengthType.DEFINITE_SHORT;
					
				}
			}else{
				////System.out.println("prepareLenghts: " + root.node);
				l = getChildrenLength(root);
				root.node.length = l;
				tmp = calculateLength(root.node.length);
				root.lengthSize = tmp.lengthSize;
				root.lengthType = tmp.lengthType;
			}
		}		
		for(int i = 0; i<root.children.size(); i++) prepareLenghts(root.children.get(i));
		
	}
	
	
	public byte[] encode(BerTag btag){
		byte[] res = null;
		byte[] tmp;
		int tag;
		////System.out.println(btag.node + ":" + btag.hasData);
		ArrayList<Byte> buff = new ArrayList<Byte>();
		tag = btag.node.asn_class.getId() + (btag.node.asn_pc == ASNTagComplexity.Constructed ? 0x20 : 0x00) + btag.node.tag;
		
		// CHOICE has no data, present only for grouping purpose
		// only selected choice is actually encoded
		if(btag.hasData){
			//System.out.println(btag.node + ":" + btag.hasData);
			//System.out.println("CLASS: " + ASNTagClass.get(btag.node.asn_class.getId()));
			//System.out.println("PC: " + btag.node.asn_pc);
			//System.out.println("TAG: " + btag.node.tag);
			//System.out.println("COMPLETE TAG: " + String.format("%02x", tag));
			//System.out.println("LENGTH: " + btag.node.length);
			//System.out.println("LENGTH TYPE: " + btag.lengthType);
			//System.out.println("LENGTH SIZE: " + btag.lengthSize);
			//if(btag.node.value != null) System.out.println("VALUE LENGTH: " + btag.node.value.length);
			//System.out.println("BYTE_POS: " + btag.node.byte_position);
			
			// ANY type
			// use tag from decoded data, since it is not defined
			if(btag.node.tag == -2){
				btag.node.asn_class = btag.asn_class;
				btag.node.tag = btag.tag;
				btag.node.asn_pc = btag.asn_pc;
				tag = btag.node.asn_class.getId() + (btag.node.asn_pc == ASNTagComplexity.Constructed ? 0x20 : 0x00) + btag.node.tag;
				//System.out.println("DECODED TAG: " + String.format("%02x", tag)); 
				//System.out.println("DECODED PC: " + btag.asn_pc); 
				
			}

			////System.out.print(String.format("%02x", tag) + ":");
			//System.out.println("----------------------");
		
			// single byte tag
			if(btag.node.tag <= 30){
				buff.add((byte)tag);
			}else{
				tag = tag | 0x1f;
				buff.add((byte)tag);
				int req_bits = Utils.int_required_bits(btag.node.tag);
				boolean[] bits = Utils.int2bits(btag.node.tag, req_bits);
				int req_tag_bytes = (int)Math.ceil((double)req_bits/7);
				int tmp_val;
				byte[] tag_bytes = new byte[req_tag_bytes];
				for(int i = 0; i<tag_bytes.length-1; i++) tag_bytes[i] |= 0x80;
				for(int i = 0; i<req_tag_bytes-1; i++){
					tmp_val = Utils.bits2int(bits, req_bits - (req_tag_bytes - i - 1) * 7 , 7);
					tag_bytes[req_tag_bytes - i - 1] |= tmp_val;
				}
				// last one
				tmp_val = Utils.bits2int(bits, 0 , req_bits - ((req_tag_bytes - 1) * 7));
				tag_bytes[0] |= tmp_val;
				// add to buffer
				for(int i = 0; i<tag_bytes.length; i++) buff.add(tag_bytes[i]);
				
			}
			byte[] ber_l = encodeLength(btag);
			for(int i = 0; i<ber_l.length; i++) buff.add(ber_l[i]);
			
				//buff.add((byte)btag.node.length);
			// if primitive or unknown structure(no known structure of child objects)
			if(btag.children.size() == 0){
				// value encoded from original data packet
				if(btag.node.value == null){
					//for(int j = 0; j<btag.node.length; j++) buff.add(btag.node.dataPacket[btag.node.byte_position + j]);
				}else{
					// new value
					// length tags have to re-encoded
					for(int j = 0; j<btag.node.value.length; j++) buff.add(btag.node.value[j]);
				}
			}
			
		}
		// process children in case of cunstructed type(SEQUENCE)
		for(int i = 0; i<btag.children.size(); i++){
			tmp = encode(btag.children.get(i));
			for(int k = 0; k<tmp.length; k++) buff.add(tmp[k]);
		
			
		}
		// combine result
		res = new byte[buff.size()];
		for(int i = 0; i<buff.size(); i++) res[i] = buff.get(i);
		return res;
	}
	
	public ASNType decode(ASNType root, byte[] _data){
		ASNType res;
		data = _data;
		byte_position = 0;
		//leaf.clear();
		document = new BerTag();
		document.children = new ArrayList<BerTag>();
		res = decodeType(root, document);
		//System.out.println("BYTE POS: " + byte_position);
		return res;
		
	}
	
	public BerTranscoderv2(){
		//leaf = new ArrayList<BerTag>();
		buildTree = true;
		
	}
}

