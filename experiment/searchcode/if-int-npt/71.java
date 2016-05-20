package vjo.java.sun.text.resources;

//Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10) 
//Source File Name:   DateFormatZoneData.java

import java.util.Enumeration;

import vjo.java.lang.* ;

import vjo.java.util.Hashtable ;
import vjo.java.util.ResourceBundle;
import vjo.java.util.Vector ;

public class DateFormatZoneData extends ResourceBundle {

	public DateFormatZoneData() {
		lookup = null;
		keys = null;
	}

	public Object handleGetObject(String s) {
		synchronized (this) {
			if (lookup == null)
				loadLookup();
		}
		if ("localPatternChars".equals(s))
			return lookup.get(s);
		String as[] = (String[]) (String[]) lookup.get(s);
		if (as == null)
			return null;
		int i = as.length;
		String as1[] = new String[i + 1];
		as1[0] = s;
		for (int j = 0; j < i; j++)
			as1[j + 1] = as[j];

		return as1;
	}

//	 public Enumeration getKeys2() {
//		if (lookup == null) {
//			loadLookup();
//		}
//		Enumeration result = null;
//		if (parent != null) {
//			final Enumeration myKeys = keys.elements();
//			final Enumeration parentKeys = parent.getKeys();
//
//			result = new Enumeration() {
//				public boolean hasMoreElements() {
//					if (temp == null) {
//						nextElement();
//					}
//					return (temp != null);
//				}
//
//				public Object nextElement() {
//					Object returnVal = temp;
//					if (myKeys.hasMoreElements()) {
//						temp = myKeys.nextElement();
//					} else {
//						temp = null;
//						while ((temp == null) && (parentKeys.hasMoreElements())) {
//							temp = parentKeys.nextElement();
//							if (lookup.containsKey(temp))
//								temp = null;
//						}
//					}
//					return returnVal;
//				}
//
//				Object temp = null;
//			};
//		} else {
//			result = keys.elements();
//		}
//		return result;
//	} 
	public Enumeration getKeys() {
		synchronized (this) {
			if (lookup == null)
				loadLookup();
		}
		Enumeration enumeration = null;
		if (parent != null) {
			final Enumeration myKeys = keys.elements();
			final Enumeration parentKeys = parent.getKeys();
			enumeration = new Enumeration() {

				public boolean hasMoreElements() {
					if (temp == null)
						nextElement();
					return temp != null;
				}

				public Object nextElement() {
					Object obj = temp;
					if (myKeys.hasMoreElements()) {
						temp = myKeys.nextElement();
					} else {
						temp = null;
						do {
							if (temp != null || !parentKeys.hasMoreElements())
								break;
							temp = parentKeys.nextElement();
							if (lookup.containsKey(temp))
								temp = null;
						} while (true);
					}
					return obj;
				}

				Object temp;

//				final Enumeration val$myKeys;
//
//				final Enumeration val$parentKeys;
//
//				final DateFormatZoneData this$0;
//
//				{
//					this$0 = DateFormatZoneData.this;
//					myKeys = enumeration;
//					parentKeys = enumeration1;
//					super();
//					temp = null;
//				}
			};
		} else {
			enumeration = keys.elements();
		}
		return enumeration;
	}

	private synchronized void loadLookup() {
		if (lookup != null)
			return;
		Object aobj[][] = getContents();
		Hashtable hashtable = new Hashtable(aobj.length);
		Vector vector = new Vector(aobj.length);
		for (int i = 0; i < aobj.length; i++) {
			hashtable.put(aobj[i][0], aobj[i][1]);
			vector.add(aobj[i][0]);
		}

		keys = vector;
		lookup = hashtable;
	}

	public Object[][] getContents() {
		String as[] = { "Acre Time", "ACT", "Acre Summer Time", "ACST" };
		String as1[] = { "Central Standard Time (South Australia)", "CST",
				"Central Summer Time (South Australia)", "CST" };
		String as2[] = { "Argentine Time", "ART", "Argentine Summer Time",
				"ARST" };
		String as3[] = { "Alaska Standard Time", "AKST",
				"Alaska Daylight Time", "AKDT" };
		String as4[] = { "Amazon Time", "AMT", "Amazon Summer Time", "AMST" };
		String as5[] = { "Arabia Standard Time", "AST", "Arabia Daylight Time",
				"ADT" };
		String as6[] = { "Armenia Time", "AMT", "Armenia Summer Time", "AMST" };
		String as7[] = { "Atlantic Standard Time", "AST",
				"Atlantic Daylight Time", "ADT" };
		String as8[] = { "Bangladesh Time", "BDT", "Bangladesh Summer Time",
				"BDST" };
		String as9[] = { "Eastern Standard Time (Queensland)", "EST",
				"Eastern Summer Time (Queensland)", "EST" };
		String as10[] = {
				"Central Standard Time (South Australia/New South Wales)",
				"CST", "Central Summer Time (South Australia/New South Wales)",
				"CST" };
		String as11[] = { "Brasilia Time", "BRT", "Brasilia Summer Time",
				"BRST" };
		String as12[] = { "Bhutan Time", "BTT", "Bhutan Summer Time", "BTST" };
		String as13[] = { "Central African Time", "CAT",
				"Central African Summer Time", "CAST" };
		String as14[] = { "Central European Time", "CET",
				"Central European Summer Time", "CEST" };
		String as15[] = { "Chatham Standard Time", "CHAST",
				"Chatham Daylight Time", "CHADT" };
		String as16[] = { "Central Indonesia Time", "CIT",
				"Central Indonesia Summer Time", "CIST" };
		String as17[] = { "Chile Time", "CLT", "Chile Summer Time", "CLST" };
		String as18[] = { "Central Standard Time", "CST",
				"Central Daylight Time", "CDT" };
		String as19[] = { "China Standard Time", "CST", "China Daylight Time",
				"CDT" };
		String as20[] = { "Central Standard Time (Northern Territory)", "CST",
				"Central Summer Time (Northern Territory)", "CST" };
		String as21[] = { "Greenwich Mean Time", "GMT", "Irish Summer Time",
				"IST" };
		String as22[] = { "Eastern African Time", "EAT",
				"Eastern African Summer Time", "EAST" };
		String as23[] = { "Easter Is. Time", "EAST", "Easter Is. Summer Time",
				"EASST" };
		String as24[] = { "Eastern European Time", "EET",
				"Eastern European Summer Time", "EEST" };
		String as25[] = { "Eastern Greenland Time", "EGT",
				"Eastern Greenland Summer Time", "EGST" };
		String as26[] = { "Eastern Standard Time", "EST",
				"Eastern Daylight Time", "EDT" };
		String as27[] = { "Eastern Standard Time (New South Wales)", "EST",
				"Eastern Summer Time (New South Wales)", "EST" };
		String as28[] = { "Gambier Time", "GAMT", "Gambier Summer Time",
				"GAMST" };
		String as29[] = { "Greenwich Mean Time", "GMT", "Greenwich Mean Time",
				"GMT" };
		String as30[] = { "Greenwich Mean Time", "GMT", "British Summer Time",
				"BST" };
		String as31[] = { "Gulf Standard Time", "GST", "Gulf Daylight Time",
				"GDT" };
		String as32[] = { "Hawaii-Aleutian Standard Time", "HAST",
				"Hawaii-Aleutian Daylight Time", "HADT" };
		String as33[] = { "Hong Kong Time", "HKT", "Hong Kong Summer Time",
				"HKST" };
		String as34[] = { "Hawaii Standard Time", "HST",
				"Hawaii Daylight Time", "HDT" };
		String as35[] = { "Indochina Time", "ICT", "Indochina Summer Time",
				"ICST" };
		String as36[] = { "Iran Standard Time", "IRST", "Iran Daylight Time",
				"IRDT" };
		String as37[] = { "Israel Standard Time", "IST",
				"Israel Daylight Time", "IDT" };
		String as38[] = { "India Standard Time", "IST", "India Daylight Time",
				"IDT" };
		String as39[] = { "Japan Standard Time", "JST", "Japan Daylight Time",
				"JDT" };
		String as40[] = { "Korea Standard Time", "KST", "Korea Daylight Time",
				"KDT" };
		String as41[] = { "Load Howe Standard Time", "LHST",
				"Load Howe Summer Time", "LHST" };
		String as42[] = { "Marshall Islands Time", "MHT",
				"Marshall Islands Summer Time", "MHST" };
		String as43[] = { "Moscow Standard Time", "MSK",
				"Moscow Daylight Time", "MSD" };
		String as44[] = { "Mountain Standard Time", "MST",
				"Mountain Daylight Time", "MDT" };
		String as45[] = { "Malaysia Time", "MYT", "Malaysia Summer Time",
				"MYST" };
		String as46[] = { "Fernando de Noronha Time", "FNT",
				"Fernando de Noronha Summer Time", "FNST" };
		String as47[] = { "Newfoundland Standard Time", "NST",
				"Newfoundland Daylight Time", "NDT" };
		String as48[] = { "New Zealand Standard Time", "NZST",
				"New Zealand Daylight Time", "NZDT" };
		String as49[] = { "Pitcairn Standard Time", "PST",
				"Pitcairn Daylight Time", "PDT" };
		String as50[] = { "Pakistan Time", "PKT", "Pakistan Summer Time",
				"PKST" };
		String as51[] = { "Pacific Standard Time", "PST",
				"Pacific Daylight Time", "PDT" };
		String as52[] = { "South Africa Standard Time", "SAST",
				"South Africa Summer Time", "SAST" };
		String as53[] = { "Solomon Is. Time", "SBT", "Solomon Is. Summer Time",
				"SBST" };
		String as54[] = { "Singapore Time", "SGT", "Singapore Summer Time",
				"SGST" };
		String as55[] = { "Eastern Standard Time (Tasmania)", "EST",
				"Eastern Summer Time (Tasmania)", "EST" };
		String as56[] = { "Turkmenistan Time", "TMT",
				"Turkmenistan Summer Time", "TMST" };
		String as57[] = { "Ulaanbaatar Time", "ULAT",
				"Ulaanbaatar Summer Time", "ULAST" };
		String as58[] = { "Western African Time", "WAT",
				"Western African Summer Time", "WAST" };
		String as59[] = { "Western European Time", "WET",
				"Western European Summer Time", "WEST" };
		String as60[] = { "West Indonesia Time", "WIT",
				"West Indonesia Summer Time", "WIST" };
		String as61[] = { "Western Standard Time (Australia)", "WST",
				"Western Summer Time (Australia)", "WST" };
		String as62[] = { "Samoa Standard Time", "SST", "Samoa Daylight Time",
				"SDT" };
		String as63[] = { "West Samoa Time", "WST", "West Samoa Summer Time",
				"WSST" };
		String as64[] = { "Chamorro Standard Time", "ChST",
				"Chamorro Daylight Time", "ChDT" };
		String as65[] = { "Eastern Standard Time (Victoria)", "EST",
				"Eastern Summer Time (Victoria)", "EST" };
		String as66[] = { "Coordinated Universal Time", "UTC",
				"Coordinated Universal Time", "UTC" };
		String as67[] = { "Uzbekistan Time", "UZT", "Uzbekistan Summer Time",
				"UZST" };
		return (new Object[][] {
				new Object[] { "PST", as51 },
				new Object[] { "America/Los_Angeles", as51 },
				new Object[] { "MST", as44 },
				new Object[] { "America/Denver", as44 },
				new Object[] { "PNT", as44 },
				new Object[] { "America/Phoenix", as44 },
				new Object[] { "CST", as18 },
				new Object[] { "America/Chicago", as18 },
				new Object[] { "EST", as26 },
				new Object[] { "America/New_York", as26 },
				new Object[] { "IET", as26 },
				new Object[] { "America/Indianapolis", as26 },
				new Object[] { "HST", as34 },
				new Object[] { "Pacific/Honolulu", as34 },
				new Object[] { "AST", as3 },
				new Object[] { "America/Anchorage", as3 },
				new Object[] { "America/Halifax", as7 },
				new Object[] { "CNT", as47 },
				new Object[] { "America/St_Johns", as47 },
				new Object[] { "ECT", as14 },
				new Object[] { "Europe/Paris", as14 },
				new Object[] { "GMT", as29 },
				new Object[] { "Africa/Casablanca", as59 },
				new Object[] { "Asia/Jerusalem", as37 },
				new Object[] { "JST", as39 },
				new Object[] { "Asia/Tokyo", as39 },
				new Object[] { "Europe/Bucharest", as24 },
				new Object[] { "CTT", as19 },
				new Object[] { "Asia/Shanghai", as19 },
				new Object[] {
						"ACT",
						new String[] {
								"Central Standard Time (Northern Territory)",
								"CST",
								"Central Daylight Time (Northern Territory)",
								"CDT" } },
				new Object[] { "AET", as27 },
				new Object[] { "AGT", as2 },
				new Object[] { "ART", as24 },
				new Object[] { "Africa/Abidjan", as29 },
				new Object[] { "Africa/Accra", as29 },
				new Object[] { "Africa/Addis_Ababa", as22 },
				new Object[] { "Africa/Algiers", as14 },
				new Object[] { "Africa/Asmera", as22 },
				new Object[] { "Africa/Bamako", as29 },
				new Object[] { "Africa/Bangui", as58 },
				new Object[] { "Africa/Banjul", as29 },
				new Object[] { "Africa/Bissau", as29 },
				new Object[] { "Africa/Blantyre", as13 },
				new Object[] { "Africa/Brazzaville", as58 },
				new Object[] { "Africa/Bujumbura", as13 },
				new Object[] { "Africa/Cairo", as24 },
				new Object[] { "Africa/Ceuta", as14 },
				new Object[] { "Africa/Conakry", as29 },
				new Object[] { "Africa/Dakar", as29 },
				new Object[] { "Africa/Dar_es_Salaam", as22 },
				new Object[] { "Africa/Djibouti", as22 },
				new Object[] { "Africa/Douala", as58 },
				new Object[] { "Africa/El_Aaiun", as59 },
				new Object[] { "Africa/Freetown", as29 },
				new Object[] { "Africa/Gaborone", as13 },
				new Object[] { "Africa/Harare", as13 },
				new Object[] { "Africa/Johannesburg", as52 },
				new Object[] { "Africa/Kampala", as22 },
				new Object[] { "Africa/Khartoum", as22 },
				new Object[] { "Africa/Kigali", as13 },
				new Object[] { "Africa/Kinshasa", as58 },
				new Object[] { "Africa/Lagos", as58 },
				new Object[] { "Africa/Libreville", as58 },
				new Object[] { "Africa/Lome", as29 },
				new Object[] { "Africa/Luanda", as58 },
				new Object[] { "Africa/Lubumbashi", as13 },
				new Object[] { "Africa/Lusaka", as13 },
				new Object[] { "Africa/Malabo", as58 },
				new Object[] { "Africa/Maputo", as13 },
				new Object[] { "Africa/Maseru", as52 },
				new Object[] { "Africa/Mbabane", as52 },
				new Object[] { "Africa/Mogadishu", as22 },
				new Object[] { "Africa/Monrovia", as29 },
				new Object[] { "Africa/Nairobi", as22 },
				new Object[] { "Africa/Ndjamena", as58 },
				new Object[] { "Africa/Niamey", as58 },
				new Object[] { "Africa/Nouakchott", as29 },
				new Object[] { "Africa/Ouagadougou", as29 },
				new Object[] { "Africa/Porto-Novo", as58 },
				new Object[] { "Africa/Sao_Tome", as29 },
				new Object[] { "Africa/Timbuktu", as29 },
				new Object[] { "Africa/Tripoli", as24 },
				new Object[] { "Africa/Tunis", as14 },
				new Object[] { "Africa/Windhoek", as58 },
				new Object[] { "America/Adak", as32 },
				new Object[] { "America/Anguilla", as7 },
				new Object[] { "America/Antigua", as7 },
				new Object[] { "America/Araguaina", as11 },
				new Object[] { "America/Aruba", as7 },
				new Object[] {
						"America/Asuncion",
						new String[] { "Paraguay Time", "PYT",
								"Paraguay Summer Time", "PYST" } },
				new Object[] { "America/Atka", as32 },
				new Object[] { "America/Bahia", as11 },
				new Object[] { "America/Barbados", as7 },
				new Object[] { "America/Belem", as11 },
				new Object[] { "America/Belize", as18 },
				new Object[] { "America/Boa_Vista", as4 },
				new Object[] {
						"America/Bogota",
						new String[] { "Colombia Time", "COT",
								"Colombia Summer Time", "COST" } },
				new Object[] { "America/Boise", as44 },
				new Object[] { "America/Buenos_Aires", as2 },
				new Object[] { "America/Cambridge_Bay", as44 },
				new Object[] { "America/Campo_Grande", as4 },
				new Object[] { "America/Cancun", as18 },
				new Object[] {
						"America/Caracas",
						new String[] { "Venezuela Time", "VET",
								"Venezuela Summer Time", "VEST" } },
				new Object[] { "America/Catamarca", as2 },
				new Object[] {
						"America/Cayenne",
						new String[] { "French Guiana Time", "GFT",
								"French Guiana Summer Time", "GFST" } },
				new Object[] { "America/Cayman", as26 },
				new Object[] { "America/Chihuahua", as44 },
				new Object[] { "America/Cordoba", as2 },
				new Object[] { "America/Costa_Rica", as18 },
				new Object[] { "America/Cuiaba", as4 },
				new Object[] { "America/Curacao", as7 },
				new Object[] { "America/Danmarkshavn", as29 },
				new Object[] { "America/Dawson", as51 },
				new Object[] { "America/Dawson_Creek", as44 },
				new Object[] { "America/Detroit", as26 },
				new Object[] { "America/Dominica", as7 },
				new Object[] { "America/Edmonton", as44 },
				new Object[] { "America/Eirunepe", as },
				new Object[] { "America/El_Salvador", as18 },
				new Object[] { "America/Ensenada", as51 },
				new Object[] { "America/Fort_Wayne", as26 },
				new Object[] { "America/Fortaleza", as11 },
				new Object[] { "America/Glace_Bay", as7 },
				new Object[] {
						"America/Godthab",
						new String[] { "Western Greenland Time", "WGT",
								"Western Greenland Summer Time", "WGST" } },
				new Object[] { "America/Goose_Bay", as7 },
				new Object[] { "America/Grand_Turk", as26 },
				new Object[] { "America/Grenada", as7 },
				new Object[] { "America/Guadeloupe", as7 },
				new Object[] { "America/Guatemala", as18 },
				new Object[] {
						"America/Guayaquil",
						new String[] { "Ecuador Time", "ECT",
								"Ecuador Summer Time", "ECST" } },
				new Object[] {
						"America/Guyana",
						new String[] { "Guyana Time", "GYT",
								"Guyana Summer Time", "GYST" } },
				new Object[] { "America/Havana", as18 },
				new Object[] { "America/Hermosillo", as44 },
				new Object[] { "America/Indiana/Indianapolis", as26 },
				new Object[] { "America/Indiana/Knox", as26 },
				new Object[] { "America/Indiana/Marengo", as26 },
				new Object[] { "America/Indiana/Vevay", as26 },
				new Object[] { "America/Inuvik", as44 },
				new Object[] { "America/Iqaluit", as26 },
				new Object[] { "America/Jamaica", as26 },
				new Object[] { "America/Jujuy", as2 },
				new Object[] { "America/Juneau", as3 },
				new Object[] { "America/Kentucky/Louisville", as26 },
				new Object[] { "America/Kentucky/Monticello", as26 },
				new Object[] { "America/Knox_IN", as26 },
				new Object[] {
						"America/La_Paz",
						new String[] { "Bolivia Time", "BOT",
								"Bolivia Summer Time", "BOST" } },
				new Object[] {
						"America/Lima",
						new String[] { "Peru Time", "PET", "Peru Summer Time",
								"PEST" } },
				new Object[] { "America/Louisville", as26 },
				new Object[] { "America/Maceio", as11 },
				new Object[] { "America/Managua", as18 },
				new Object[] { "America/Manaus", as4 },
				new Object[] { "America/Martinique", as7 },
				new Object[] { "America/Mazatlan", as44 },
				new Object[] { "America/Mendoza", as2 },
				new Object[] { "America/Menominee", as18 },
				new Object[] { "America/Merida", as18 },
				new Object[] { "America/Mexico_City", as18 },
				new Object[] {
						"America/Miquelon",
						new String[] { "Pierre & Miquelon Standard Time",
								"PMST", "Pierre & Miquelon Daylight Time",
								"PMDT" } },
				new Object[] {
						"America/Montevideo",
						new String[] { "Uruguay Time", "UYT",
								"Uruguay Summer Time", "UYST" } },
				new Object[] { "America/Monterrey", as18 },
				new Object[] { "America/Montreal", as26 },
				new Object[] { "America/Montserrat", as7 },
				new Object[] { "America/Nassau", as26 },
				new Object[] { "America/Nipigon", as26 },
				new Object[] { "America/Nome", as3 },
				new Object[] { "America/Noronha", as46 },
				new Object[] { "America/North_Dakota/Center", as18 },
				new Object[] { "America/Panama", as26 },
				new Object[] { "America/Pangnirtung", as26 },
				new Object[] {
						"America/Paramaribo",
						new String[] { "Suriname Time", "SRT",
								"Suriname Summer Time", "SRST" } },
				new Object[] { "America/Port-au-Prince", as26 },
				new Object[] { "America/Port_of_Spain", as7 },
				new Object[] { "America/Porto_Acre", as },
				new Object[] { "America/Porto_Velho", as4 },
				new Object[] { "America/Puerto_Rico", as7 },
				new Object[] { "America/Rainy_River", as18 },
				new Object[] { "America/Rankin_Inlet", as18 },
				new Object[] { "America/Recife", as11 },
				new Object[] { "America/Regina", as18 },
				new Object[] { "America/Rio_Branco", as },
				new Object[] { "America/Rosario", as2 },
				new Object[] { "America/Santiago", as17 },
				new Object[] { "America/Santo_Domingo", as7 },
				new Object[] { "America/Sao_Paulo", as11 },
				new Object[] { "America/Scoresbysund", as25 },
				new Object[] { "America/Shiprock", as44 },
				new Object[] { "America/St_Kitts", as7 },
				new Object[] { "America/St_Lucia", as7 },
				new Object[] { "America/St_Thomas", as7 },
				new Object[] { "America/St_Vincent", as7 },
				new Object[] { "America/Swift_Current", as18 },
				new Object[] { "America/Tegucigalpa", as18 },
				new Object[] { "America/Thule", as7 },
				new Object[] { "America/Thunder_Bay", as26 },
				new Object[] { "America/Tijuana", as51 },
				new Object[] { "America/Toronto", as26 },
				new Object[] { "America/Tortola", as7 },
				new Object[] { "America/Vancouver", as51 },
				new Object[] { "America/Virgin", as7 },
				new Object[] { "America/Whitehorse", as51 },
				new Object[] { "America/Winnipeg", as18 },
				new Object[] { "America/Yakutat", as3 },
				new Object[] { "America/Yellowknife", as44 },
				new Object[] { "Antarctica/Casey", as61 },
				new Object[] {
						"Antarctica/Davis",
						new String[] { "Davis Time", "DAVT",
								"Davis Summer Time", "DAVST" } },
				new Object[] {
						"Antarctica/DumontDUrville",
						new String[] { "Dumont-d'Urville Time", "DDUT",
								"Dumont-d'Urville Summer Time", "DDUST" } },
				new Object[] {
						"Antarctica/Mawson",
						new String[] { "Mawson Time", "MAWT",
								"Mawson Summer Time", "MAWST" } },
				new Object[] { "Antarctica/McMurdo", as48 },
				new Object[] { "Antarctica/Palmer", as17 },
				new Object[] {
						"Antarctica/Rothera",
						new String[] { "Rothera Time", "ROTT",
								"Rothera Summer Time", "ROTST" } },
				new Object[] { "Antarctica/South_Pole", as48 },
				new Object[] {
						"Antarctica/Syowa",
						new String[] { "Syowa Time", "SYOT",
								"Syowa Summer Time", "SYOST" } },
				new Object[] {
						"Antarctica/Vostok",
						new String[] { "Vostok Time", "VOST",
								"Vostok Summer Time", "VOSST" } },
				new Object[] { "Arctic/Longyearbyen", as14 },
				new Object[] { "Asia/Aden", as5 },
				new Object[] {
						"Asia/Almaty",
						new String[] { "Alma-Ata Time", "ALMT",
								"Alma-Ata Summer Time", "ALMST" } },
				new Object[] { "Asia/Amman", as24 },
				new Object[] {
						"Asia/Anadyr",
						new String[] { "Anadyr Time", "ANAT",
								"Anadyr Summer Time", "ANAST" } },
				new Object[] {
						"Asia/Aqtau",
						new String[] { "Aqtau Time", "AQTT",
								"Aqtau Summer Time", "AQTST" } },
				new Object[] {
						"Asia/Aqtobe",
						new String[] { "Aqtobe Time", "AQTT",
								"Aqtobe Summer Time", "AQTST" } },
				new Object[] { "Asia/Ashgabat", as56 },
				new Object[] { "Asia/Ashkhabad", as56 },
				new Object[] { "Asia/Baghdad", as5 },
				new Object[] { "Asia/Bahrain", as5 },
				new Object[] {
						"Asia/Baku",
						new String[] { "Azerbaijan Time", "AZT",
								"Azerbaijan Summer Time", "AZST" } },
				new Object[] { "Asia/Bangkok", as35 },
				new Object[] { "Asia/Beirut", as24 },
				new Object[] {
						"Asia/Bishkek",
						new String[] { "Kirgizstan Time", "KGT",
								"Kirgizstan Summer Time", "KGST" } },
				new Object[] {
						"Asia/Brunei",
						new String[] { "Brunei Time", "BNT",
								"Brunei Summer Time", "BNST" } },
				new Object[] { "Asia/Calcutta", as38 },
				new Object[] {
						"Asia/Choibalsan",
						new String[] { "Choibalsan Time", "CHOT",
								"Choibalsan Summer Time", "CHOST" } },
				new Object[] { "Asia/Chongqing", as19 },
				new Object[] { "Asia/Chungking", as19 },
				new Object[] {
						"Asia/Colombo",
						new String[] { "Sri Lanka Time", "LKT",
								"Sri Lanka Summer Time", "LKST" } },
				new Object[] { "Asia/Dacca", as8 },
				new Object[] { "Asia/Dhaka", as8 },
				new Object[] {
						"Asia/Dili",
						new String[] { "East Timor Time", "TPT",
								"East Timor Summer Time", "TPST" } },
				new Object[] { "Asia/Damascus", as24 },
				new Object[] { "Asia/Dubai", as31 },
				new Object[] {
						"Asia/Dushanbe",
						new String[] { "Tajikistan Time", "TJT",
								"Tajikistan Summer Time", "TJST" } },
				new Object[] { "Asia/Gaza", as24 },
				new Object[] { "Asia/Harbin", as19 },
				new Object[] { "Asia/Hong_Kong", as33 },
				new Object[] {
						"Asia/Hovd",
						new String[] { "Hovd Time", "HOVT", "Hovd Summer Time",
								"HOVST" } },
				new Object[] {
						"Asia/Irkutsk",
						new String[] { "Irkutsk Time", "IRKT",
								"Irkutsk Summer Time", "IRKST" } },
				new Object[] { "Asia/Istanbul", as24 },
				new Object[] { "Asia/Jakarta", as60 },
				new Object[] {
						"Asia/Jayapura",
						new String[] { "East Indonesia Time", "EIT",
								"East Indonesia Summer Time", "EIST" } },
				new Object[] {
						"Asia/Kabul",
						new String[] { "Afghanistan Time", "AFT",
								"Afghanistan Summer Time", "AFST" } },
				new Object[] {
						"Asia/Kamchatka",
						new String[] { "Petropavlovsk-Kamchatski Time", "PETT",
								"Petropavlovsk-Kamchatski Summer Time", "PETST" } },
				new Object[] { "Asia/Karachi", as50 },
				new Object[] { "Asia/Kashgar", as19 },
				new Object[] {
						"Asia/Katmandu",
						new String[] { "Nepal Time", "NPT",
								"Nepal Summer Time", "NPST" } },
				new Object[] {
						"Asia/Krasnoyarsk",
						new String[] { "Krasnoyarsk Time", "KRAT",
								"Krasnoyarsk Summer Time", "KRAST" } },
				new Object[] { "Asia/Kuala_Lumpur", as45 },
				new Object[] { "Asia/Kuching", as45 },
				new Object[] { "Asia/Kuwait", as5 },
				new Object[] { "Asia/Macao", as19 },
				new Object[] { "Asia/Macau", as19 },
				new Object[] {
						"Asia/Magadan",
						new String[] { "Magadan Time", "MAGT",
								"Magadan Summer Time", "MAGST" } },
				new Object[] { "Asia/Makassar", as16 },
				new Object[] {
						"Asia/Manila",
						new String[] { "Philippines Time", "PHT",
								"Philippines Summer Time", "PHST" } },
				new Object[] { "Asia/Muscat", as31 },
				new Object[] { "Asia/Nicosia", as24 },
				new Object[] {
						"Asia/Novosibirsk",
						new String[] { "Novosibirsk Time", "NOVT",
								"Novosibirsk Summer Time", "NOVST" } },
				new Object[] {
						"Asia/Oral",
						new String[] { "Oral Time", "ORAT", "Oral Summer Time",
								"ORAST" } },
				new Object[] {
						"Asia/Omsk",
						new String[] { "Omsk Time", "OMST", "Omsk Summer Time",
								"OMSST" } },
				new Object[] { "Asia/Phnom_Penh", as35 },
				new Object[] { "Asia/Pontianak", as60 },
				new Object[] { "Asia/Pyongyang", as40 },
				new Object[] { "Asia/Qatar", as5 },
				new Object[] {
						"Asia/Qyzylorda",
						new String[] { "Qyzylorda Time", "QYZT",
								"Qyzylorda Summer Time", "QYZST" } },
				new Object[] {
						"Asia/Rangoon",
						new String[] { "Myanmar Time", "MMT",
								"Myanmar Summer Time", "MMST" } },
				new Object[] { "Asia/Riyadh", as5 },
				new Object[] { "Asia/Saigon", as35 },
				new Object[] {
						"Asia/Sakhalin",
						new String[] { "Sakhalin Time", "SAKT",
								"Sakhalin Summer Time", "SAKST" } },
				new Object[] { "Asia/Samarkand", as67 },
				new Object[] { "Asia/Seoul", as40 },
				new Object[] { "Asia/Singapore", as54 },
				new Object[] { "Asia/Taipei", as19 },
				new Object[] { "Asia/Tel_Aviv", as37 },
				new Object[] { "Asia/Tashkent", as67 },
				new Object[] {
						"Asia/Tbilisi",
						new String[] { "Georgia Time", "GET",
								"Georgia Summer Time", "GEST" } },
				new Object[] { "Asia/Tehran", as36 },
				new Object[] { "Asia/Thimbu", as12 },
				new Object[] { "Asia/Thimphu", as12 },
				new Object[] { "Asia/Ujung_Pandang", as16 },
				new Object[] { "Asia/Ulaanbaatar", as57 },
				new Object[] { "Asia/Ulan_Bator", as57 },
				new Object[] { "Asia/Urumqi", as19 },
				new Object[] { "Asia/Vientiane", as35 },
				new Object[] {
						"Asia/Vladivostok",
						new String[] { "Vladivostok Time", "VLAT",
								"Vladivostok Summer Time", "VLAST" } },
				new Object[] {
						"Asia/Yakutsk",
						new String[] { "Yakutsk Time", "YAKT",
								"Yaktsk Summer Time", "YAKST" } },
				new Object[] {
						"Asia/Yekaterinburg",
						new String[] { "Yekaterinburg Time", "YEKT",
								"Yekaterinburg Summer Time", "YEKST" } },
				new Object[] { "Asia/Yerevan", as6 },
				new Object[] {
						"Atlantic/Azores",
						new String[] { "Azores Time", "AZOT",
								"Azores Summer Time", "AZOST" } },
				new Object[] { "Atlantic/Bermuda", as7 },
				new Object[] { "Atlantic/Canary", as59 },
				new Object[] {
						"Atlantic/Cape_Verde",
						new String[] { "Cape Verde Time", "CVT",
								"Cape Verde Summer Time", "CVST" } },
				new Object[] { "Atlantic/Faeroe", as59 },
				new Object[] { "Atlantic/Jan_Mayen", as14 },
				new Object[] { "Atlantic/Madeira", as59 },
				new Object[] { "Atlantic/Reykjavik", as29 },
				new Object[] {
						"Atlantic/South_Georgia",
						new String[] { "South Georgia Standard Time", "GST",
								"South Georgia Daylight Time", "GDT" } },
				new Object[] { "Atlantic/St_Helena", as29 },
				new Object[] {
						"Atlantic/Stanley",
						new String[] { "Falkland Is. Time", "FKT",
								"Falkland Is. Summer Time", "FKST" } },
				new Object[] { "Australia/ACT", as27 },
				new Object[] { "Australia/Adelaide", as1 },
				new Object[] { "Australia/Brisbane", as9 },
				new Object[] { "Australia/Broken_Hill", as10 },
				new Object[] { "Australia/Canberra", as27 },
				new Object[] { "Australia/Darwin", as20 },
				new Object[] { "Australia/Hobart", as55 },
				new Object[] { "Australia/LHI", as41 },
				new Object[] { "Australia/Lindeman", as9 },
				new Object[] { "Australia/Lord_Howe", as41 },
				new Object[] { "Australia/Melbourne", as65 },
				new Object[] { "Australia/North", as20 },
				new Object[] { "Australia/NSW", as27 },
				new Object[] { "Australia/Perth", as61 },
				new Object[] { "Australia/Queensland", as9 },
				new Object[] { "Australia/South", as1 },
				new Object[] { "Australia/Sydney", as27 },
				new Object[] { "Australia/Tasmania", as55 },
				new Object[] { "Australia/Victoria", as65 },
				new Object[] { "Australia/West", as61 },
				new Object[] { "Australia/Yancowinna", as10 },
				new Object[] { "BET", as11 },
				new Object[] { "BST", as8 },
				new Object[] { "Brazil/Acre", as },
				new Object[] { "Brazil/DeNoronha", as46 },
				new Object[] { "Brazil/East", as11 },
				new Object[] { "Brazil/West", as4 },
				new Object[] { "Canada/Atlantic", as7 },
				new Object[] { "Canada/Central", as18 },
				new Object[] { "Canada/East-Saskatchewan", as18 },
				new Object[] { "Canada/Eastern", as26 },
				new Object[] { "Canada/Mountain", as44 },
				new Object[] { "Canada/Newfoundland", as47 },
				new Object[] { "Canada/Pacific", as51 },
				new Object[] { "Canada/Yukon", as51 },
				new Object[] { "Canada/Saskatchewan", as18 },
				new Object[] { "CAT", as13 },
				new Object[] { "CET", as14 },
				new Object[] { "Chile/Continental", as17 },
				new Object[] { "Chile/EasterIsland", as23 },
				new Object[] { "CST6CDT", as18 },
				new Object[] { "Cuba", as18 },
				new Object[] { "EAT", as22 },
				new Object[] { "EET", as24 },
				new Object[] { "Egypt", as24 },
				new Object[] { "Eire", as21 },
				new Object[] { "EST5EDT", as26 },
				new Object[] { "Etc/Greenwich", as29 },
				new Object[] { "Etc/UCT", as66 },
				new Object[] { "Etc/Universal", as66 },
				new Object[] { "Etc/UTC", as66 },
				new Object[] { "Etc/Zulu", as66 },
				new Object[] { "Europe/Amsterdam", as14 },
				new Object[] { "Europe/Andorra", as14 },
				new Object[] { "Europe/Athens", as24 },
				new Object[] { "Europe/Belfast", as30 },
				new Object[] { "Europe/Belgrade", as14 },
				new Object[] { "Europe/Berlin", as14 },
				new Object[] { "Europe/Bratislava", as14 },
				new Object[] { "Europe/Brussels", as14 },
				new Object[] { "Europe/Budapest", as14 },
				new Object[] { "Europe/Chisinau", as24 },
				new Object[] { "Europe/Copenhagen", as14 },
				new Object[] { "Europe/Dublin", as21 },
				new Object[] { "Europe/Gibraltar", as14 },
				new Object[] { "Europe/Helsinki", as24 },
				new Object[] { "Europe/Istanbul", as24 },
				new Object[] { "Europe/Kaliningrad", as24 },
				new Object[] { "Europe/Kiev", as24 },
				new Object[] { "Europe/Lisbon", as59 },
				new Object[] { "Europe/Ljubljana", as14 },
				new Object[] { "Europe/London", as30 },
				new Object[] { "Europe/Luxembourg", as14 },
				new Object[] { "Europe/Madrid", as14 },
				new Object[] { "Europe/Malta", as14 },
				new Object[] { "Europe/Minsk", as24 },
				new Object[] { "Europe/Monaco", as14 },
				new Object[] { "Europe/Moscow", as43 },
				new Object[] { "Europe/Nicosia", as24 },
				new Object[] { "Europe/Oslo", as14 },
				new Object[] { "Europe/Prague", as14 },
				new Object[] { "Europe/Riga", as24 },
				new Object[] { "Europe/Rome", as14 },
				new Object[] {
						"Europe/Samara",
						new String[] { "Samara Time", "SAMT",
								"Samara Summer Time", "SAMST" } },
				new Object[] { "Europe/San_Marino", as14 },
				new Object[] { "Europe/Sarajevo", as14 },
				new Object[] { "Europe/Simferopol", as24 },
				new Object[] { "Europe/Skopje", as14 },
				new Object[] { "Europe/Sofia", as24 },
				new Object[] { "Europe/Stockholm", as14 },
				new Object[] { "Europe/Tallinn", as24 },
				new Object[] { "Europe/Tirane", as14 },
				new Object[] { "Europe/Tiraspol", as24 },
				new Object[] { "Europe/Uzhgorod", as24 },
				new Object[] { "Europe/Vaduz", as14 },
				new Object[] { "Europe/Vatican", as14 },
				new Object[] { "Europe/Vienna", as14 },
				new Object[] { "Europe/Vilnius", as24 },
				new Object[] { "Europe/Warsaw", as14 },
				new Object[] { "Europe/Zagreb", as14 },
				new Object[] { "Europe/Zaporozhye", as24 },
				new Object[] { "Europe/Zurich", as14 },
				new Object[] { "GB", as30 },
				new Object[] { "GB-Eire", as30 },
				new Object[] { "Greenwich", as29 },
				new Object[] { "Hongkong", as33 },
				new Object[] { "Iceland", as29 },
				new Object[] { "Iran", as36 },
				new Object[] { "IST", as38 },
				new Object[] { "Indian/Antananarivo", as22 },
				new Object[] {
						"Indian/Chagos",
						new String[] { "Indian Ocean Territory Time", "IOT",
								"Indian Ocean Territory Summer Time", "IOST" } },
				new Object[] {
						"Indian/Christmas",
						new String[] { "Christmas Island Time", "CXT",
								"Christmas Island Summer Time", "CXST" } },
				new Object[] {
						"Indian/Cocos",
						new String[] { "Cocos Islands Time", "CCT",
								"Cocos Islands Summer Time", "CCST" } },
				new Object[] { "Indian/Comoro", as22 },
				new Object[] {
						"Indian/Kerguelen",
						new String[] {
								"French Southern & Antarctic Lands Time",
								"TFT",
								"French Southern & Antarctic Lands Summer Time",
								"TFST" } },
				new Object[] {
						"Indian/Mahe",
						new String[] { "Seychelles Time", "SCT",
								"Seychelles Summer Time", "SCST" } },
				new Object[] {
						"Indian/Maldives",
						new String[] { "Maldives Time", "MVT",
								"Maldives Summer Time", "MVST" } },
				new Object[] {
						"Indian/Mauritius",
						new String[] { "Mauritius Time", "MUT",
								"Mauritius Summer Time", "MUST" } },
				new Object[] { "Indian/Mayotte", as22 },
				new Object[] {
						"Indian/Reunion",
						new String[] { "Reunion Time", "RET",
								"Reunion Summer Time", "REST" } },
				new Object[] { "Israel", as37 },
				new Object[] { "Jamaica", as26 },
				new Object[] { "Japan", as39 },
				new Object[] { "Kwajalein", as42 },
				new Object[] { "Libya", as24 },
				new Object[] {
						"MET",
						new String[] { "Middle Europe Time", "MET",
								"Middle Europe Summer Time", "MEST" } },
				new Object[] { "Mexico/BajaNorte", as51 },
				new Object[] { "Mexico/BajaSur", as44 },
				new Object[] { "Mexico/General", as18 },
				new Object[] { "MIT", as63 },
				new Object[] { "MST7MDT", as44 },
				new Object[] { "Navajo", as44 },
				new Object[] { "NET", as6 },
				new Object[] { "NST", as48 },
				new Object[] { "NZ", as48 },
				new Object[] { "NZ-CHAT", as15 },
				new Object[] { "PLT", as50 },
				new Object[] { "Portugal", as59 },
				new Object[] { "PRT", as7 },
				new Object[] { "Pacific/Apia", as63 },
				new Object[] { "Pacific/Auckland", as48 },
				new Object[] { "Pacific/Chatham", as15 },
				new Object[] { "Pacific/Easter", as23 },
				new Object[] {
						"Pacific/Efate",
						new String[] { "Vanuatu Time", "VUT",
								"Vanuatu Summer Time", "VUST" } },
				new Object[] {
						"Pacific/Enderbury",
						new String[] { "Phoenix Is. Time", "PHOT",
								"Phoenix Is. Summer Time", "PHOST" } },
				new Object[] {
						"Pacific/Fakaofo",
						new String[] { "Tokelau Time", "TKT",
								"Tokelau Summer Time", "TKST" } },
				new Object[] {
						"Pacific/Fiji",
						new String[] { "Fiji Time", "FJT", "Fiji Summer Time",
								"FJST" } },
				new Object[] {
						"Pacific/Funafuti",
						new String[] { "Tuvalu Time", "TVT",
								"Tuvalu Summer Time", "TVST" } },
				new Object[] {
						"Pacific/Galapagos",
						new String[] { "Galapagos Time", "GALT",
								"Galapagos Summer Time", "GALST" } },
				new Object[] { "Pacific/Gambier", as28 },
				new Object[] { "Pacific/Guadalcanal", as53 },
				new Object[] { "Pacific/Guam", as64 },
				new Object[] { "Pacific/Johnston", as34 },
				new Object[] {
						"Pacific/Kiritimati",
						new String[] { "Line Is. Time", "LINT",
								"Line Is. Summer Time", "LINST" } },
				new Object[] {
						"Pacific/Kosrae",
						new String[] { "Kosrae Time", "KOST",
								"Kosrae Summer Time", "KOSST" } },
				new Object[] { "Pacific/Kwajalein", as42 },
				new Object[] { "Pacific/Majuro", as42 },
				new Object[] {
						"Pacific/Marquesas",
						new String[] { "Marquesas Time", "MART",
								"Marquesas Summer Time", "MARST" } },
				new Object[] { "Pacific/Midway", as62 },
				new Object[] {
						"Pacific/Nauru",
						new String[] { "Nauru Time", "NRT",
								"Nauru Summer Time", "NRST" } },
				new Object[] {
						"Pacific/Niue",
						new String[] { "Niue Time", "NUT", "Niue Summer Time",
								"NUST" } },
				new Object[] {
						"Pacific/Norfolk",
						new String[] { "Norfolk Time", "NFT",
								"Norfolk Summer Time", "NFST" } },
				new Object[] {
						"Pacific/Noumea",
						new String[] { "New Caledonia Time", "NCT",
								"New Caledonia Summer Time", "NCST" } },
				new Object[] { "Pacific/Pago_Pago", as62 },
				new Object[] {
						"Pacific/Palau",
						new String[] { "Palau Time", "PWT",
								"Palau Summer Time", "PWST" } },
				new Object[] { "Pacific/Pitcairn", as49 },
				new Object[] {
						"Pacific/Ponape",
						new String[] { "Ponape Time", "PONT",
								"Ponape Summer Time", "PONST" } },
				new Object[] {
						"Pacific/Port_Moresby",
						new String[] { "Papua New Guinea Time", "PGT",
								"Papua New Guinea Summer Time", "PGST" } },
				new Object[] {
						"Pacific/Rarotonga",
						new String[] { "Cook Is. Time", "CKT",
								"Cook Is. Summer Time", "CKST" } },
				new Object[] { "Pacific/Saipan", as64 },
				new Object[] { "Pacific/Samoa", as62 },
				new Object[] {
						"Pacific/Tahiti",
						new String[] { "Tahiti Time", "TAHT",
								"Tahiti Summer Time", "TAHST" } },
				new Object[] {
						"Pacific/Tarawa",
						new String[] { "Gilbert Is. Time", "GILT",
								"Gilbert Is. Summer Time", "GILST" } },
				new Object[] {
						"Pacific/Tongatapu",
						new String[] { "Tonga Time", "TOT",
								"Tonga Summer Time", "TOST" } },
				new Object[] {
						"Pacific/Truk",
						new String[] { "Truk Time", "TRUT", "Truk Summer Time",
								"TRUST" } },
				new Object[] {
						"Pacific/Wake",
						new String[] { "Wake Time", "WAKT", "Wake Summer Time",
								"WAKST" } },
				new Object[] {
						"Pacific/Wallis",
						new String[] { "Wallis & Futuna Time", "WFT",
								"Wallis & Futuna Summer Time", "WFST" } },
				new Object[] {
						"Pacific/Yap",
						new String[] { "Yap Time", "YAPT", "Yap Summer Time",
								"YAPST" } }, new Object[] { "Poland", as14 },
				new Object[] { "PRC", as19 }, new Object[] { "PST8PDT", as51 },
				new Object[] { "ROK", as40 },
				new Object[] { "Singapore", as54 },
				new Object[] { "SST", as53 },
				new Object[] { "SystemV/AST4", as7 },
				new Object[] { "SystemV/AST4ADT", as7 },
				new Object[] { "SystemV/CST6", as18 },
				new Object[] { "SystemV/CST6CDT", as18 },
				new Object[] { "SystemV/EST5", as26 },
				new Object[] { "SystemV/EST5EDT", as26 },
				new Object[] { "SystemV/HST10", as34 },
				new Object[] { "SystemV/MST7", as44 },
				new Object[] { "SystemV/MST7MDT", as44 },
				new Object[] { "SystemV/PST8", as49 },
				new Object[] { "SystemV/PST8PDT", as51 },
				new Object[] { "SystemV/YST9", as28 },
				new Object[] { "SystemV/YST9YDT", as3 },
				new Object[] { "Turkey", as24 }, new Object[] { "UCT", as66 },
				new Object[] { "Universal", as66 },
				new Object[] { "US/Alaska", as3 },
				new Object[] { "US/Aleutian", as32 },
				new Object[] { "US/Arizona", as44 },
				new Object[] { "US/Central", as18 },
				new Object[] { "US/Eastern", as26 },
				new Object[] { "US/Hawaii", as34 },
				new Object[] { "US/Indiana-Starke", as26 },
				new Object[] { "US/East-Indiana", as26 },
				new Object[] { "US/Michigan", as26 },
				new Object[] { "US/Mountain", as44 },
				new Object[] { "US/Pacific", as51 },
				new Object[] { "US/Pacific-New", as51 },
				new Object[] { "US/Samoa", as62 },
				new Object[] { "UTC", as66 }, new Object[] { "VST", as35 },
				new Object[] { "W-SU", as43 }, new Object[] { "WET", as59 },
				new Object[] { "Zulu", as66 },
				new Object[] { "localPatternChars", "GyMdkHmsSEDFwWahKzZ" } });
	}

	private static final String LOCALPATTERNCHARS = "localPatternChars";

	Hashtable lookup;

	Vector keys;
}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\opt\java5-ibm-2007-12-13\jre\lib\core.jar
	Total time: 172 ms
	Jad reported messages/errors:
Overlapped try statements detected. Not all exception handlers will be resolved in the method handleGetObject
Overlapped try statements detected. Not all exception handlers will be resolved in the method getKeys
	Exit status: 0
	Caught exceptions:
*/

