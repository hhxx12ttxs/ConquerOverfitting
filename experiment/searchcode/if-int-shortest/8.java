package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.cbio.causality.data.portal.GeneticProfile;
import org.cbio.causality.idmapping.HGNC;
import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.cbio.causality.model.Change;
import org.eclipse.swt.graphics.Color;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.TextUtilities;
import org.gvt.ChisioMain;
import org.gvt.command.CreateCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.model.NodeModel;
import org.gvt.util.EntityHolder;
import org.gvt.util.NodeProvider;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;
import org.patika.mada.util.XRef;

import java.text.NumberFormat;
import java.util.*;

/**
 * Any node to use in BioPAX visual graph.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class BioPAXNode extends NodeModel implements IBioPAXL3Node
{
	/**
	 * Unique ID of this node.
	 */
	private int id;

	private List<XRef> references;
	private List<XRef> secondaryReferences;

	protected NodeUtil util;

	public BioPAXNode(CompoundModel root)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();

		this.references = new ArrayList<XRef>();
		this.secondaryReferences = new ArrayList<XRef>();
		this.util = new NodeUtil(this);
	}

	/**
	 * Constructor for excising.
	 * @param excised original graph member
	 */
	public BioPAXNode(BioPAXNode excised, CompoundModel root)
	{
		this(root);
		this.id = excised.id;
		this.references.addAll(excised.getReferences());
		this.setShape(excised.getShape());
		this.setSize(excised.getSize());
		this.setColor(excised.getColor());
		this.setText(excised.getText());
		this.setTextColor(excised.getTextColor());
		this.setTooltipText(excised.getTooltipText());

		for (Object key : excised.getAllLabels())
		{
			this.putLabel(key, excised.getLabel(key));
		}

		getGraph().putInExcisionMap(excised, this);
		this.putLabel(BioPAXL3Graph.EXCISED_FROM, excised);
	}

	public boolean isComplexMember()
	{
		return false;
	}

	/**
	 * Empty method will be overwritten in children when the node needes to be configured by the
	 * properties of its corresponding biopax model obejects.
	 */
	public void configFromModel()
	{
		// Assume there is no configuration needed.
	}

	public BioPAXL3Graph getGraph()
	{
		return util.getGraph();
	}

	public Compartment getCompartment()
	{
		return util.getCompartment();
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.getText();
	}

	public NodeUtil getUtil()
	{
		return util;
	}

	public List<XRef> getReferences()
	{
		return this.references;
	}

	public void addReference(XRef ref)
	{
		if (!this.references.contains(ref))
		{
			this.references.add(ref);
		}
	}

	@Override
	public List<XRef> getSecondaryReferences()
	{
		return secondaryReferences;
	}

	public void addSecondaryReference(XRef ref)
	{
		if (!this.secondaryReferences.contains(ref))
		{
			this.secondaryReferences.add(ref);
		}
	}

	/**
	 * Extract cross-references from the based entity.
	 * @return list of possible names
	 */
	protected String extractReferences(Entity ent)
	{
		List<String> list = new ArrayList<String>();

		if (ent.getStandardName() != null && ent.getStandardName().length() > 0)
		{
			this.addReference(new XRef(NAME_REF, ent.getStandardName()));
			if (!list.contains(ent.getStandardName())) list.add(ent.getStandardName());
		}

		for (String nm : ent.getName())
		{
			this.addReference(new XRef(NAME_REF, nm));
			if (!list.contains(nm)) list.add(nm);
		}

//		for (xref xr : new ClassFilterSet<unificationXref>(ent.getXREF(), unificationXref.class))
		for (Xref xr : ent.getXref())
		{
			if (xr != null)
			{
				this.addReference(new XRef(xr));
			}
		}

		if (ent instanceof SimplePhysicalEntity)
		{
			SimplePhysicalEntity spe = (SimplePhysicalEntity) ent;
			EntityReference er = spe.getEntityReference();
			if (er != null)
			{
				for (Xref xr : er.getXref())
				{
					this.addSecondaryReference(new XRef(xr));
				}

				for (String name : er.getName())
				{
					this.addSecondaryReference(new XRef("Name", name));
				}
			}
		}

		String names = "";

		if (!list.isEmpty())
		{
			names = list.get(0);

			for (int i = 1; i < list.size(); i++)
			{
				names += "\n" + list.get(i);
			}
		}

		return names;
	}

	protected int suggestInitialWidth()
	{
		Dimension dim = TextUtilities.INSTANCE.getStringExtents(getText(), getTextFont());
		int width = Math.min(dim.width + 4, MAX_INITIAL_WIDTH);
		return width;
	}

	/**
	 * Searches references and finds the shortest name reference.
	 * @return first shortest name reference
	 */
	public String getShortestNameRef()
	{
		int shortest = Integer.MAX_VALUE;
		String name = null;

		for (XRef ref : references)
		{
			if (ref.getDb().equals(NAME_REF))
			{
				String value = ref.getRef();

				if (value.length() < shortest)
				{
					name = value;
					shortest = value.length();
				}
			}
		}
		return name;
	}

	public static String getDisplayName(Entity ent)
	{
		String txt = null;

		if (ent instanceof SmallMolecule)
		{
			EntityReference er = ((SmallMolecule) ent).getEntityReference();
			if (er != null && er.getDisplayName() != null)
			{
				txt = er.getDisplayName();
			}
		}

		if (txt == null) txt = extractGeneSymbol(ent);
		if (txt == null) txt = ent.getDisplayName();

		if (txt == null)
		{
			for (String name : ent.getName())
			{
				if (txt == null || (name != null && name.length() < txt.length()))
				{
					txt = name;
				}
			}

			if (ent.getStandardName() != null &&
				(txt == null || txt.length() > ent.getStandardName().length()))
			{
				txt = ent.getStandardName();
			}
		}

		return txt;
	}

	public static String extractGeneSymbol(EntityHolder holder)
	{
		String s = null;
		if (holder.l3er != null) s = extractGeneSymbol(holder.l3er);
		if (s == null && holder.l3pe != null) extractGeneSymbol(holder.l3pe);
		return s;
	}

	public static String extractGeneSymbol(BioPAXElement ent)
	{
		String sym = null;

		if (ent instanceof XReferrable)
		{
			Set<Xref> set = new HashSet<Xref>(((XReferrable) ent).getXref());

			if (ent instanceof SimplePhysicalEntity &&
				((SimplePhysicalEntity) ent).getEntityReference() != null)
			{
				set.addAll(((SimplePhysicalEntity) ent).getEntityReference().getXref());
			}

			for (Xref xref : set)
			{
				if (xref.getDb() != null && xref.getDb().toLowerCase().startsWith("hgnc"))
				{
					String id = xref.getId();
					if (id == null) continue;

					sym = HGNC.getSymbol(id);

					if (sym != null) break;
				}
			}
			if (sym == null)
			{
				for (Xref ref : set)
				{
					String db = ref.getDb();

					if (db == null) continue;

					if (db.equalsIgnoreCase("GENE_SYMBOL") ||
						db.equalsIgnoreCase("GENESYMBOL") ||
						db.equalsIgnoreCase("GENE SYMBOL") ||
						db.equalsIgnoreCase("HGNC SYMBOL") ||
						db.equalsIgnoreCase("GENE-SYMBOL") ||
						db.equalsIgnoreCase("SYMBOL"))
					{
						sym = ref.getId();
						break;
					}
				}
			}
		}

		return sym;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Location related
	//----------------------------------------------------------------------------------------------

	public boolean fetchLocation(String pathwayRDFID)
	{
		return util.fetchLocation(pathwayRDFID);
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void recordLocation()
	{
		util.recordLocation();
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void eraseLocation()
	{
		util.eraseLocation();
	}

	/**
	 * A node is assumed to map unique biopax model element by default and id hash is not used. This
	 * method must be overwritten in children when mapping clashes occur, e.g. when drawing two
	 * conversions for in chisio for representing a reversible conversion in biopax.
	 */
	public String getIDHash()
	{
		return util.getIDHash();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Model tagging
	//----------------------------------------------------------------------------------------------

	public boolean hasModelTag(String tag)
	{
		return util.hasModelTag(tag);
	}

	public String fetchModelTag(String tag)
	{
		return util.fetchModelTag(tag);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Experiment data related
	//----------------------------------------------------------------------------------------------

	public Representable getRepresentableData(Object key)
	{
		return (Representable) this.getLabel(key);
	}

	public ExperimentData getExperimentData(String type)
	{
		return (ExperimentData) this.getLabel(type);
	}

	public void setExperimentData(ExperimentData data)
	{
		this.putLabel(data.getKey(), data);
	}

	public boolean hasExperimentData(Object key)
	{
		return this.hasLabel(key);
	}

	public boolean hasSignificantExperimentalChange(String type)
	{
		return this.hasExperimentData(type) && this.getExperimentData(type).isSignificant();
	}

	public int getExperimentDataSign(String type)
	{
		return this.getExperimentData(type).getSign();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Traversing
	//----------------------------------------------------------------------------------------------

	public Collection<? extends Node> getParents()
	{
		CompoundModel p = this.getParentModel();
		Collection<Node> col = new ArrayList<Node>(1);

		if (p == null || p.isRoot())
		{
			return col;
		}
		else
		{
			col.add((BioPAXCompoundNode) p);
			return col;
		}
	}

	public Collection<? extends Edge> getUpstream()
	{
		return this.getTargetConnections();
	}

	public Collection<? extends Edge> getDownstream()
	{
		return this.getSourceConnections();
	}

	public Collection<? extends Node> getChildren()
	{
		return new ArrayList<Node>(0);
	}

	public boolean sameEntity(Node n)
	{
		return n.equals(this);
	}

	/**
	 * No node is a breadth node by default.
	 * @return false by default
	 */
	public boolean isBreadthNode()
	{
		return false;
	}

	public String toString()
	{
		return this.getName();
	}

	public boolean isHighlighted()
	{
		return this.isHighlight();
	}

	/**
	 * Nothing is a transcriptional event by default. Will be overwritten in some children.
	 * @return false by default
	 */
	public boolean isTranscriptionEvent()
	{
		return false;
	}

	public Set<Node> getTabuNodes()
	{
		return Collections.emptySet();
	}

	public Set<GraphObject> getRequisites()
	{
		HashSet<GraphObject> reqs = new HashSet<GraphObject>();

		if (this.getParentModel() instanceof Compartment)
		{
			reqs.add((GraphObject) this.getParentModel());
		}

		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		return list;
	}

	public Color getStringSpecificColor(String name)
	{
		int id = Math.abs(name.hashCode());

		int number1 = Math.abs((name + name).hashCode() * 97) % 1000;
		int number2 = Math.abs((name + name).hashCode() * 59) % 1000;

		int t = 350;
		int c[] = new int[3];
		c[0] = number1 % Math.min(255, t);
		c[1] = number2 % Math.min(255, t - c[0]);
		c[2] = Math.min(254, t - c[0] - c[1]);

		int p = 255;

		int r = c[0] + (id % (p - c[0]));
		int g = c[1] + (((id % p) * 97) % (p - c[1]));
		int b = c[2] + (((id % p) * 59) % (p - c[2]));

		return new Color(null, r, g, b);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Inspector related
	//----------------------------------------------------------------------------------------------

	/**
	 * Writes the set of open controlled vocabulary in one line of a string.
	 * @param set
	 * @return
	 */
	public static String formatInString(Set<? extends ControlledVocabulary> set)
	{
		String s = "";

		for (ControlledVocabulary voc : set)
		{
			if (s.length() > 0) s += ", ";
			s += voc;
		}
		return s;
	}

	/**
	 * Converts the class name to a prontable string in the inspector.
	 * @param clsName
	 * @return
	 */
	public static String classNameToString(String clsName)
	{
		clsName = clsName.substring(clsName.lastIndexOf(".")+1, clsName.lastIndexOf("I"));
		clsName = clsName.substring(0, 1).toUpperCase() + clsName.substring(1);

		for (int i = 3; i < clsName.length(); i++)
		{
			if (Character.isLowerCase(clsName.charAt(i - 1)) &&
				Character.isUpperCase(clsName.charAt(i)))
			{
				clsName = clsName.substring(0, i) + " " + clsName.substring(i);
			}
		}

		return clsName;
	}

	/**
	 * Parses the type, name and synomyms of the given entity into the properties list, which can be
	 * displayed in the inspector.
	 * @param list
	 * @param ent
	 */
	public static void addNamesAndTypeAndID(List<String[]> list, Named ent)
	{
		assert ent != null;

		String type = BioPAXNode.classNameToString(ent.getClass().getName());
		list.add(new String[]{"Type", type});

		String s = ent.getDisplayName();
		if (s != null && s.length() > 0)
		{
			list.add(new String[]{"Disp. Name", s});
		}

		s = ent.getStandardName();
		if (s != null && s.length() > 0)
		{
			list.add(new String[]{"St. Name", s});
		}

		for (String synon : ent.getName())
		{
			if (synon != null && synon.length() > 0)
			{
				list.add(new String[]{"Name", synon});
			}
		}

		list.add(new String[]{"ID", ent.getRDFId()});
	}

	/**
	 * Parses the data source, xref, commecnt and organism of the given entity into the properties
	 * list, which can be displayed in the inspector.
	 * @param list
	 * @param ent
	 */
	public static void addDataSourceAndXrefAndComments(List<String[]> list, Entity ent)
	{
		for (Provenance ds : ent.getDataSource())
		{
			if (!ds.getName().isEmpty())
			{
				list.add(new String[]{"Data Source", ds.toString()});
			}
		}

		for (Xref xr : ent.getXref())
		{
			list.add(new String[]{"Reference", xr.toString()});
		}

		if (ent instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) ent).getEntityReference();

			if (er instanceof SequenceEntityReference)
			{
				BioSource src = ((SequenceEntityReference) er).getOrganism();

				if (src != null)
				{
					list.add(new String[]{"Organism", src.toString()});
				}
			}
		}

		for (String comment : ent.getComment())
		{
			if (!comment.contains("@Layout"))
			{
				String c;

				boolean first = true;

				while (comment.length() > PROPERTY_CHAR_LIMIT)
				{
					int cutIndex = comment.lastIndexOf(" ", PROPERTY_CHAR_LIMIT);
					if (cutIndex < 0) cutIndex = PROPERTY_CHAR_LIMIT;

					c = comment.substring(0, cutIndex);
					comment = comment.substring(cutIndex).trim();

					String prop = first ? "Comment" : "";
					list.add(new String[]{prop, c});
					first = false;
				}

				if (comment.trim().length() > 0)
				{
					String prop = first ? "Comment" : "";
					list.add(new String[]{prop, comment.trim()});
				}
			}
		}
	}

	protected void createControlOverInteraction(CompoundModel root, Interaction inter,
		NodeProvider prov)
	{
		// Create effectors.

		for (Control con : inter.getControlledOf())
		{
			if (ChbControl.controlNeedsToBeANode(con, prov))
			{
				ChbControl ctrl = (ChbControl) prov.getNode(con.getRDFId(), root);
				new EffectorSecondHalf(ctrl, this, con);
			}
			else
			{
				for (Controller ctrlr : con.getController())
				{
					NodeModel source = prov.getNode(ctrlr.getRDFId(), root);
					if (source != null) new NonModulatedEffector(source, this, con, inter);
				}
			}
		}
	}

	public static final String NAME_REF = "Name";
	public static final int PROPERTY_CHAR_LIMIT = 50;
	public static final int MAX_INITIAL_WIDTH = 100;

	public List<String[]> getCBioDataInspectable(ChisioMain main)
	{
		List<String[]> list = new ArrayList<String[]>();
		String geneName = null;

		if (this instanceof EntityAssociated)
		{
			EntityHolder ent = ((EntityAssociated) this).getEntity();
			if (ent.l3er != null)
			{
				for (Xref xr : ent.l3er.getXref())
				{
					// Remember the latest gene name
					if (xr instanceof RelationshipXref)
					{
						if (xr.getDb() != null && xr.getDb().toLowerCase().startsWith("hgnc"))
						{
							geneName = HGNC.getSymbol(xr.getId());
							if (geneName != null) break;
						}
					}
				}
			}
		}

		if (geneName == null)
		{
			geneName = HGNC.getSymbol(getText());
		}

		// Add the following statistics only if we got Portal data, otherwise skip it
		CBioPortalAccessor portalAccessor = ChisioMain.cBioPortalAccessor;
		if (portalAccessor != null
			&& !portalAccessor.getCurrentGeneticProfiles().isEmpty()
			&& geneName != null)
		{

			// Add data profile details
			list.add(new String[]{"Cancer study", portalAccessor.getCurrentCancerStudy().getName()});
			String profilesStr = "";
			for (GeneticProfile geneticProfile : portalAccessor.getCurrentGeneticProfiles())
			{
				profilesStr += geneticProfile.getName() + "; ";
			}
			profilesStr = profilesStr.substring(0, profilesStr.length() - 2);
			list.add(new String[]{"Data profiles", profilesStr});
			list.add(new String[]{"Case set", portalAccessor.getCurrentCaseList().getDescription()});

			// This will hit the cache, so no worries on the speed or connection status
			AlterationPack alterations = portalAccessor.getAlterations(geneName);

			int sampleSize = alterations.get(Alteration.ANY).length;
			list.add(new String[]{"Number of samples", sampleSize + ""});

			for (Alteration alt : Alteration.values())
			{
				if (alt.isSummary() && alt != Alteration.ANY) continue;
				Change[] changes = alterations.get(alt);
				if (changes == null) continue;

				int activating, inhibiting, unknownChange, stayInactive, noChange, noData;
				activating = inhibiting = unknownChange = stayInactive = noChange = noData = 0;

				List<Integer> expIndices = main.getExperimentDataManager(
					ExperimentData.CBIOPORTAL_ALTERATION_DATA).getFirstExpIndices();

				int i = 0;
				for (Change change : changes)
				{
					if (!expIndices.contains(i++))
						continue;

					switch (change)
					{
						case INHIBITING:
							inhibiting++;
							break;
						case ACTIVATING:
							activating++;
							break;
						case UNKNOWN_CHANGE:
							unknownChange++;
							break;
						case NO_CHANGE:
							noChange++;
							break;
						case NO_DATA:
							noData++;
							break;
						case STAY_INACTIVE:
							stayInactive++;
							break;
					}
				}

				int totalChange = inhibiting + activating + unknownChange;

				// Skip if no alteration of current type exists
				if (totalChange == 0) continue;

				NumberFormat n = NumberFormat.getPercentInstance();
				n.setMaximumFractionDigits(1);

				list.add(new String[]{alt.getName() + " freq",
					n.format(totalChange / (double) sampleSize)});
				if (activating > 0) list.add(new String[]{" - Activating", activating + ""});
				if (inhibiting > 0) list.add(new String[]{" - Inhibiting", inhibiting + ""});
				if (unknownChange > 0)
					list.add(new String[]{" - Unknown change", unknownChange + ""});
				// We don't have "stays inactive" in cBio Portal data. So skipping it.
				//list.add(new String[]{" - Stays inactive", n.format(inactive)});
				if (noData > 0) list.add(new String[]{" - No data", noData + ""});
//				list.add(new String[]{"No change", n.format(noChange)});
			}
		}

		return list;
	}
}

