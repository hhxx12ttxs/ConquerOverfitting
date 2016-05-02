/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.xmlcml.cml.parsetree.oscar;

import java.util.List;
import nu.xom.Attribute;
import org.xmlcml.cml.parsetree.*;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.parsetree.Sentence.POS;
import org.xmlcml.cml.parsetree.amount.Cd;

/**
 *
 * @author pm286
 */
public class OscarCm extends ParseNode {

    public final static String TAG = "oscar-cm";

    public OscarCm(Element element) {
        super(element);
    }

    public String getTag() {return TAG;}

    public static void normalizeSentence(Sentence sentence) {
        concatenateTrailingNumbers(sentence.getDelegateElement());
        siblingOscar(sentence.getDelegateElement());
        mixturesFromDashes(sentence.getDelegateElement());
        moleculesFromText(sentence.getDelegateElement());
        removeNesting(sentence.getDelegateElement());
        flattenOscarMolecule(sentence.getDelegateElement());

    }

    //<oscar-cm><oscar-cm>H</oscar-cm><cd>2</cd><oscar-cm>O</oscar-cm>...</oscar-cm>
    private static void concatenateTrailingNumbers(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).concatenateTrailingNumbersAndRemoveNesting();
        }
    }

    private void concatenateTrailingNumbersAndRemoveNesting() {
        int parity = 0;
        String oscarString = "";
        int nchild = this.delegateElement.getChildCount();
        for (int i = 0; i < nchild; i++) {
            Node child = this.delegateElement.getChild(i);
            if (!(child instanceof Element)) {
                oscarString = null;
                break;
            }
            Element childElement = (Element) child;
            String elementName = childElement.getLocalName();
            if (parity == 0 && elementName.equals(this.getTag())) {
                oscarString += childElement.getValue();
            } else if (parity == 1 && elementName.equals(Cd.TAG)) {
                oscarString += childElement.getValue();
            } else {
                oscarString = null;
                break;
            }
            parity = 1-parity;
        }
        if (oscarString != null) {
            this.delegateElement.removeChildren();
            setText(this.delegateElement, oscarString);
            addPOSToDelegate(POS.NP);
//            printDelegate("concat numbers");
            // catalog these later...
        }
    }

    //<oscar-cm><oscar-cm>H</oscar-cm><cd>2</cd><oscar-cm>O</oscar-cm>...</oscar-cm>
    private static void mixturesFromDashes(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).mixturesFromDashes();
        }
    }

    private boolean mixturesFromDashes() {
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm><oscar-cm/><dash/><oscar-cm/></oscar-cm>");
        if (nodeList.size() > 0) {
            CMLMolecule molecule1 = new CMLMolecule();
            molecule1.setTitle(this.delegateElement.getChild(0).getValue());
            CMLMolecule molecule2 = new CMLMolecule();
            molecule2.setTitle(this.delegateElement.getChild(2).getValue());
            this.delegateElement.removeChildren();
            this.delegateElement.appendChild(molecule1);
            this.delegateElement.appendChild(molecule2);
            addPOSToDelegate(POS.NP);
            this.delegateElement.addAttribute(new Attribute("role", "mixture"));
            // catalog these later...
        }
        return nodeList.size() > 0;
    }

    //<oscar-cm><oscar-cm>H</oscar-cm><cd>2</cd><oscar-cm>O</oscar-cm>...</oscar-cm>
    private static void siblingOscar(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).siblingOscar();
        }
    }

    private boolean siblingOscar() {
        // this may be an artefact
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm><oscar-cm/><oscar-cm/></oscar-cm>");
        if (nodeList.size() > 0) {
            /*
            CMLMolecule molecule1 = new CMLMolecule();
            molecule1.setTitle(this.delegateElement.getChild(0).getValue());
            CMLMolecule molecule2 = new CMLMolecule();
            molecule2.setTitle(this.delegateElement.getChild(2).getValue());
            this.delegateElement.removeChildren();
            this.delegateElement.appendChild(molecule1);
            this.delegateElement.appendChild(molecule2);
             */
            addPOSToDelegate(POS.NP);
            this.delegateElement.addAttribute(new Attribute("role", "mixture"));
            // catalog these later...
        }
        return nodeList.size() > 0;
    }


    //<oscar-cm>water</oscar-cm>
    private static void moleculesFromText(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).moleculesFromText();
        }
    }

    private boolean moleculesFromText() {
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm>water</oscar-cm>");
        if (nodeList.size() == 1 && delegateElement.getChildElements().size() == 0) {
            CMLMolecule molecule = new CMLMolecule();
            molecule.setTitle(delegateElement.getChild(0).getValue());
            delegateElement.removeChildren();
            delegateElement.appendChild(molecule);
            addPOSToDelegate(POS.NP);
            this.delegateElement.addAttribute(new Attribute("role", "molecule"));
            // catalog these later...
        }
        return nodeList.size() > 0;
    }

    //<oscar-cm><oscar-cm/></oscar-cm>
    private static void removeNesting(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).removeNesting();
        }
    }
    private boolean removeNesting() {
//<oscar-cm><oscar-cm /></oscar-cm>
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm><oscar-cm /></oscar-cm>");
        if (nodeList.size() > 0) {
            nodeList.get(0).detach();
            CMLUtil.copyAttributes((Element)nodeList.get(0), delegateElement);
            CMLUtil.transferChildren((Element)nodeList.get(0), delegateElement);
            addPOSToDelegate(POS.NP);
            printDelegate("denested");
            // catalog these later...
        }
        return nodeList.size() > 0;
    }

    //<oscar-cm>molecule</oscar-cm>
    private static void flattenOscarMolecule(Element element) {
        Nodes nodes = element.query(".//*[local-name()='"+TAG+"']");
        for (int i = 0; i < nodes.size(); i++) {
            new OscarCm((Element)nodes.get(i)).flattenOscarMolecule();
        }
    }
    private boolean flattenOscarMolecule() {
//<oscar-cm><molecule/></oscar-cm>
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm><molecule/></oscar-cm>");
        if (nodeList.size() > 0) {
            Element molecule = (Element) nodeList.get(0);
            molecule.detach();
            addPOSToDelegate(POS.NP);
            CMLUtil.copyAttributes(delegateElement, molecule);
            CMLUtil.transferChildren(delegateElement, molecule);
            ParentNode parent = delegateElement.getParent();
            parent.replaceChild(delegateElement, molecule);
        }
        return nodeList.size() > 0;
    }

//<oscar-cm><oscar-cm>water</oscar-cm><dash>/</dash><oscar-cm>methanol</oscar-cm></oscar-cm>

    //<oscar-cm><oscar-cm /><dash /><oscar-cm /></oscar-cm>
    private boolean processTrailingDash() {
        List<Node> nodeList = getTemplatedNodesFromDelegateChildren("<oscar-cm><oscar-cm /><dash /></oscar-cm>");
        if (nodeList.size() > 0) {
            nodeList.get(1).detach();
            CMLUtil.transferChildren((Element)nodeList.get(0), delegateElement);
            addPOSToDelegate(POS.NP);
            printDelegate("denested");
            // catalog these later...
        }
        return nodeList.size() > 0;
    }

}

//<oscar-cm><oscar-cm /><dash /><oscar-cm /></oscar-cm>}

