package a10.s100502012;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class FrameWork extends JFrame{
	private String[] flagTitles={
		"Canada","China","England","German","USA"	
	};
	
	private ImageIcon[] flagImage={
		new ImageIcon("image/Canada.gif"),
		new ImageIcon("image/China.gif"),
		new ImageIcon("image/England.gif"),
		new ImageIcon("image/German.gif"),
		new ImageIcon("image/USA.gif")
	};
	
	private String[] flagDescription=new String[5];

	private JComboBox jcbo=new JComboBox(flagTitles);
	
	private DescriptionPanel dp=new DescriptionPanel();
	
	private JButton jbHistogram=new JButton("Show Histogram");
	
	private JFrame histogramFrame=new JFrame();
	
	private Histogram histogram=new Histogram();
	
	private Clock c = new Clock();
	
	JTextArea jta;
	
	
	public FrameWork(){
		flagDescription[0]="Canada is a North American country consisting of ten provinces and three territories" +
				          ". Located in the northern part of the continent, it extends from the Atlantic Ocean in the east to the" +
				          " Pacific Ocean in the west, and northward into the Arctic Ocean. Spanning over 9.9 million square kilomet" +
				          "res, Canada is the world's second-largest country by total area, and its common border with the United States" +
				          " is the longest land border in the world.The land that is now Canada has been inhabited for millennia by various" +
				          " Aboriginal peoples. Beginning in the late 15th century, British and French expeditions explored, and later " +
				          "settled, along the region's Atlantic coast. France ceded nearly all of its colonies in North America in 1763 after" +
				          " the Seven Years' War. In 1867, with the union of three British North American colonies through Confederation, " +
				          "Canada was formed as a federal dominion of four provinces. This began an accretion of provinces and territories " +
				          "and a process of increasing autonomy from the United Kingdom. This widening autonomy was highlighted by the Balfour " +
				          "Declaration of 1926 and reaffirmed by the Statute of Westminster of 1931, which declared self-governing dominions " +
				          "within the British Empire to be equal. The Canada Act of 1982 finally severed the vestiges of legal dependence on the" +
				          " British Parliament.Canada is a federal state that is governed as a parliamentary democracy and a constitutional" +
				          " monarchy with Queen Elizabeth II as head of state. It is a bilingual nation with both English and French as official" +
				          " languages at the federal level. Canada's diversified economy is one of the world's largest, and is reliant upon its " +
				          "abundant natural resources and upon trade ?V particularly with the United States, with which Canada has had a long " +
				          "and complex relationship. It is a member of the G7, G8, G20, NATO, OECD, WTO, Commonwealth of Nations, Francophonie, " +
				          "OAS, APEC, and UN. With the sixth-highest Human Development Index and ninth-highest per capita income globally, " +
				          "Canada's standard of living is one of the world's highest.";
		
		flagDescription[1]="China , officially the People's Republic of China (PRC), is the world's most-populous country, with a population of over" +
				          " 1.3 billion. The East Asian state covers approximately 9.6 million square kilometres, and is the world's " +
				          "second-largest country by land area,[13] and the third- or fourth-largest in total area, depending on the " +
				          "definition of total area.[14]The People's Republic of China is a single-party state governed by the Communist " +
				          "Party of China.[15] It exercises jurisdiction over 22 provinces, five autonomous regions, four directly controlled " +
				          "municipalities (Beijing, Tianjin, Shanghai, and Chongqing), and two mostly self-governing[16] special " +
				          "administrativeregions (SARs), Hong Kong and Macau. Its capital city is Beijing.[17] The PRC also claims Taiwan," +
				          " which is controlled by the Republic of China (ROC)?Xa separate political entity?Xas its 23rd province, a claim " +
				          "controversial due to the complex political status of Taiwan and the unresolved Chinese Civil War. The PRC " +
				          "government denies the legitimacy of the ROC China's landscape is vast and diverse, with forest steppes and " +
				          "the Gobi and Taklamakan deserts occupying the arid north and northwest near Mongolia and Central Asia, and" +
				          " subtropical forests being prevalent in the wetter south near Southeast Asia. The terrain of western China " +
				          "is rugged and elevated, with the Himalaya, Karakoram, Pamir and Tian Shan mountain ranges separating China " +
				          "from South and Central Asia. The world's apex, Mt. Everest (8,848 m), lies on the China?VNepal border, while " +
				          "the world's second-highest point, K2 (8,611 m), is situated on China's border with Pakistan. The country's " +
				          "lowest and the world's third-lowest point, Lake Ayding (?154 m), is located in the Turpan Depression. The " +
				          "Yangtze and Yellow Rivers, the third- and sixth-longest in the world, have their sources in the Tibetan Plateau " +
				          "and continue to the densely populated eastern seaboard. China's coastline along the Pacific Ocean is 14,500 " +
				          "kilometres (9,000 mi) long?Xthe 11th-longest in the world?Xand is bounded by the Bohai, Yellow, East and South " +
				          "China Seas.The nation of China has had numerous historical incarnations. The ancient Chinese civilization?Xone " +
				          "of the world's earliest?Xflourished in the fertile basin of the Yellow River in the North China Plain.[18] China's " +
				          "political system was based on hereditary monarchies, known as dynasties, beginning with the semi-mythological Xia " +
				          "of the Yellow River basin (approx. 2000 BC) and ending with the fall of the Qing Dynasty in 1911. Since 221 BC, " +
				          "when the Qin Dynasty first conquered several states to form a Chinese empire, the country has expanded, fractured " +
				          "and been reformed numerous times. The Republic of China, founded in 1911 after the overthrow of the Qing dynasty, " +
				          "ruled the Chinese mainland until 1949. In 1945, the ROC acquired Taiwan from Japan following World War II." +
				          "In the 1946?V1949 phase of the Chinese Civil War, the Chinese Communist Party defeated the nationalist" +
				          " Kuomintang in mainland China and established the People's Republic of China in Beijing on 1 October 1949." +
				          " The Kuomintang relocated the ROC government to Taiwan, establishing its capital in Taipei. The ROC's " +
				          "jurisdiction is now limited to Taiwan and several outlying islands, including Penghu, Kinmen and Matsu. " +
				          "Since 1949, the People's Republic of China and the Republic of China (now widely known as Taiwan)" +
				          " have remained in dispute over the sovereignty of China and the political status of Taiwan," +
				          " mutually claiming each other's territory and competing for international diplomatic recognition." +
				          " In 1971, the PRC gained admission to the United Nations and took the Chinese seat as a permanent" +
				          " member of the U.N. Security Council. China is also a member of numerous formal and informal " +
				          "multilateral organizations, including the WTO, APEC, BRICS, the Shanghai Cooperation Organisation," +
				          " BCIM and the G-20. As of September 2011, all but 23 countries have recognized the PRC as the sole " +
				          "legitimate government of China.Since the introduction of market-based economic reforms in 1978," +
				          " China has become the world's fastest-growing major economy.[19] As of 2012, it is the world's " +
				          "second-largest economy, after the United States, by both nominal GDP and purchasing power parity " +
				          "(PPP),[20] and is also the world's largest exporter and second-largest importer of goods. On per " +
				          "capita terms, China ranked 90th by nominal GDP and 91st by GDP (PPP) in 2011, according to the IMF. China" +
				          " is a recognized nuclear weapons state and has the world's largest standing army, with the second-largest" +
				          " defense budget. In 2003, China became the third nation in the world, after the former Soviet Union and the" +
				          " United States, to independently launch a successful manned space mission. China has been characterized as " +
				          "a potential superpower by a number of academics,[21] military analysts, and public policy and economics analysts.";
		
		flagDescription[2]="Germany , officially the Federal Republic of Germany (German: Bundesrepublik Deutschland, pronounced , is a " +
				          "federal parliamentary republic in Europe. The country consists of 16 states while the capital and largest city is Berlin" +
				          ". Germany covers an area of 357,021 km2 and has a largely temperate seasonal climate. With 81.8 million inhabitants, it " +
				          "is the most populous member state and the largest economy in the European Union. It is one of the major political powers " +
				          "of the European continent and a technological leader in many fields.A region named Germania, inhabited by several Germanic" +
				          " peoples, was documented before AD 100. During the Migration Period, the Germanic tribes expanded southward, and " +
				          "established successor kingdoms throughout much of Europe. Beginning in the 10th century, German territories formed " +
				          "a central part of the Holy Roman Empire.[6] During the 16th century, northern German regions became the centre of the" +
				          " Protestant Reformation while southern and western parts remained dominated by Roman Catholic denominations, with the" +
				          " two factions clashing in the Thirty Years' War, marking the beginning of the Catholic?VProtestant divide that has" +
				          " characterized German society ever since.[7] Occupied during the Napoleonic Wars, the rise of Pan-Germanism inside " +
				          "the German Confederation resulted in the unification of most of the German states into the German Empire in 1871 which" +
				          " was Prussian dominated. After the German Revolution of 1918?V1919 and the subsequent military surrender in World War I," +
				          " the Empire was replaced by the Weimar Republic in 1918, and partitioned in the Treaty of Versailles. Amidst the Great " +
				          "Depression, the Third Reich was proclaimed in 1933. The latter period was marked by Fascism and World War II. After " +
				          "1945, Germany was divided by allied occupation, and evolved into two states, East Germany and West Germany. In 1990 " +
				          "Germany was reunified.Germany was a founding member of the European Community in 1957, which became the EU in" +
				          " 1993. It is part of the Schengen Area and since 1999 a member of the eurozone. Germany is a Great Power and " +
				          "member of the United Nations, NATO, the G8, the G20, the OECD and the Council of Europe, and took a non-permanent" +
				          " seat on the UN Security Council for the 2011?V2012 term.It has the world's fourth largest economy by nominal " +
				          "GDP and the fifth largest by purchasing power parity. It is the second largest exporter and third largest importer " +
				          "of goods. The country has developed a very high standard of living and a comprehensive system of social security." +
				          " Germany has been the home of many influential scientists and inventors, and is known for its cultural and " +
				          "political history.";
		
		flagDescription[3]="The United Kingdom of Great Britain and Northern Ireland[nb 5] (commonly known as the United Kingdom, the UK, " +
				          "or Britain) is a sovereign state located off the north-western coast of continental Europe. The country includes" +
				          " the island of Great Britain, the north-eastern part of the island of Ireland and many smaller islands. " +
				          "Northern Ireland is the only part of the UK that shares a land border with another sovereign state?Xthe " +
				          "Republic of Ireland. Apart from this land border the UK is surrounded by the Atlantic Ocean, the North Sea," +
				          " the English Channel and the Irish Sea.The United Kingdom is a unitary state governed under a constitutional " +
				          "monarchy and a parliamentary system, with its seat of government in the capital city of London. It is a" +
				          " country in its own right[10] and consists of four countries: England, Northern Ireland, Scotland and Wales." +
				          " There are three devolved administrations, each with varying powers,[11][12] based in Belfast, Edinburgh and" +
				          " Cardiff, the capitals of Northern Ireland, Scotland, and Wales. Associated with the UK, but not " +
				          "constitutionally part of it, are three Crown Dependencies.[13] The United Kingdom has fourteen overseas " +
				          "territories.[14] These are remnants of the British Empire which, at its height in 1922, encompassed almost " +
				          "a quarter of the world's land surface and was the largest empire in history. British influence can still be " +
				          "observed in the language, culture and legal systems of many of its former territories.The UK is a developed " +
				          "country and has the world's seventh-largest economy by nominal GDP and eighth-largest economy by purchasing " +
				          "power parity. It was the world's first industrialised country[15] and the world's foremost power during the " +
				          "19th and early 20th centuries.[16] The UK remains a great power with leading economic, cultural, military, " +
				          "scientific and political influence.[17] It is a recognised nuclear weapons state and its military expenditure " +
				          "ranks fourth in the world.[18]The UK has been a permanent member of the United Nations Security " +
				          "Council since its first session in 1946 and has been a member of the European Union and its predecessor t" +
				          "he European Economic Community since 1973. It is also a member of the Commonwealth of Nations, " +
				          "the Council of Europe, the G7, the G8, the G20, NATO, the OECD and the World Trade Organization.";
		
		flagDescription[4]="The United States of America (commonly abbreviated to the United States, the U.S., the USA, America, and the " +
				          "States) is a federal constitutional republic comprising fifty states and a federal district. The country is" +
				          "situated mostly in central North America, where its forty-eight contiguous states and Washington, D.C., " +
				          "the capital district,lie between the Pacific and Atlantic Oceans, bordered by Canada to the north and " +
				          "Mexico to the south. The state of Alaska is in the northwest of the continent, with Canada to the east " +
				          "and Russia to the west, across the Bering Strait. The state of Hawaii is an archipelago in the mid-Pacific. " +
				          "The country also possesses several territories in the Pacific and Caribbean. At 3.79 million square " +
				          "miles (9.83 million km2) and with over 312 million people, the United States is the third or fourth " +
				          "largest country by total area, and the third largest by both land area and population. It is one of " +
				          "the world's most ethnically diverse and multicultural nations, the product of large-scale immigration from " +
				          "many countries.[6] The U.S. economy is the world's largest national economy, with an estimated 2011 GDP of" +
				          " $15.1 trillion (22% of nominal global GDP and over 19% of global GDP at purchasing-power parity).[3][7] " +
				          "Per capita income is the world's sixth-highest.[3]digenous peoples descended from forebears who migrated" +
				          " from Asia have inhabited what is now the mainland United States for many thousands of years. This Native" +
				          " American population was greatly reduced by disease and warfare after European contact. The United States " +
				          "was founded by thirteen British colonies located along the Atlantic seaboard. On July 4, 1776, they issued " +
				          "the Declaration of Independence, which proclaimed their right to self-determination and their establishment " +
				          "of a cooperative union. The rebellious states defeated the British Empire in the American Revolution, " +
				          "the first successful colonial war of independence.[8] The current United States Constitution was adopted" +
				          " on September 17, 1787; its ratification the following year made the states part of a single republic " +
				          "with a stronger central government. The Bill of Rights, comprising ten constitutional amendments" +
				          " guaranteeing many fundamental civil rights and freedoms, was ratified in 1791.Through the 19th century," +
				          " the United States displaced native tribes, acquired the Louisiana territory from France, Florida from Spain," +
				          " part of the Oregon Country from the United Kingdom, Alta California and New Mexico from Mexico, and Alaska " +
				          "from Russia, and annexed the Republic of Texas and the Republic of Hawaii. Disputes between the agrarian " +
				          "South and industrial North over the expansion of the institution of slavery and states' rights provoked t" +
				          "he Civil War of the 1860s. The North's victory prevented a permanent split of the country and led to the end " +
				          "of legal slavery in the United States. By the 1870s, its national economy was the world's largest.[9] The " +
				          "Spanish?VAmerican War and World War I confirmed the country's status as a military power. It emerged from " +
				          "World War II as the first country with nuclear weapons and a permanent member of the United Nations " +
				          "Security Council. The end of the Cold War and the dissolution of the Soviet Union left the United " +
				          "States as the sole superpower. The country accounts for 41% of global military spending,[10] and is a " +
				          "leading economic, political, and cultural force in the world.[11]";
		
		
		setDisplay(0);
		
		
		
		jbHistogram.setFont(new Font("SanSerif",Font.BOLD,16));
		
		add(jcbo,BorderLayout.NORTH);
		jcbo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				setDisplay(jcbo.getSelectedIndex());
			}
		});
		
		add(jbHistogram,BorderLayout.SOUTH);
		jbHistogram.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int[] count=counterLetters();
				histogram.showHistogram(count);
				histogramFrame.setSize(700,350);
				histogramFrame.setTitle("Histogram");
				histogramFrame.setLocationRelativeTo(rootPane);
				histogramFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				histogramFrame.setVisible(true);
				
			}
		});
		
		add(dp);
		histogramFrame.add(histogram);
		histogramFrame.setLayout(new GridLayout(1,1));
	}
	
	public void setDisplay(int index){// displaying the flag's name ,image,and its description 
		dp.setTitle(flagTitles[index]);
		dp.setImageIcon(flagImage[index]);
		dp.setDescription(flagDescription[index]);
		
		
	}
	
	public String getText(){
		return flagDescription[jcbo.getSelectedIndex()];
	}
	
	private int[] counterLetters(){
		int[] count=new int[26];
		String text=getText();
		
		for(int i=0 ; i<text.length() ; i++){// algorism of getting the number of an alphbet in the article
			char character=text.charAt(i);
			if ((character>='A') && (character<='Z')){
				count[character-'A']++;
			}
			else if((character>='a') && (character<='z')){
                count[character-'a']++;				
			}
		}
		
		return count;
	}
	
}

