import java.io.*;
import java.text.MessageFormat;
import java.util.*;

public class MonoMainA {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String inputFormat = br.readLine();
        String outputFormat = br.readLine();

        AbstractReader reader = ReaderFactory.createReader(inputFormat, br);
        FileTree tree = reader.read();
        AbstractWriter writer = WriterFactory.createWriter(outputFormat, tree, System.out);
        writer.write();
    }
}

enum FormatType {
    find,
    python,
    acm1,
    acm2,
    acm3,
    xml
}


class FileTree {

    private SystemNode rootNode;
    private int nodeCount;

    public SystemNode getRootNode() {
        return rootNode;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setRootNode(SystemNode rootNode) {
        this.rootNode = rootNode;
        nodeCount++;
    }

    /**
     * add child to parent node
     * FIND format
     *
     * @param child
     * @param parents
     */
    public void addChild(SystemNode child, String[] parents) {
        if (parents != null) {
            SystemNode parentNode = findParentNode(new TreeSet<SystemNode>() {{
                add(rootNode);
            }}, parents);
            child.setParent(parentNode);
            parentNode.addChild(child);
            nodeCount++;
        }
    }

    private SystemNode findParentNode(Set<SystemNode> currentLevelNodes, String[] parents) {
        SystemNode parentNode = null;
        for (SystemNode node : currentLevelNodes) {
            if (node.getName().equals(parents[0])) {
                parentNode = node;
                break;
            }
        }
        if (parents.length > 1) {
            int length = parents.length - 1;
            String[] restParents = new String[length];
            System.arraycopy(parents, 1, restParents, 0, length);
            parentNode = findParentNode(parentNode.getChildren(), restParents);
        }

        return parentNode;
    }

    /**
     * Add child to file tree
     * PYTHON format
     *
     * @param node
     * @param nodeStack
     */
    public void addChild(SystemNode node, List<SystemNode> nodeStack) {
        SystemNode parentNode = findParentNode(nodeStack);
        parentNode.addChild(node);
        nodeCount++;
    }

    private SystemNode findParentNode(List<SystemNode> nodeStack) {
        SystemNode parentNode = rootNode;
        for (int i = 1; i < nodeStack.size(); i++) {
            SystemNode necessaryNode = nodeStack.get(i);
            for (SystemNode sn : parentNode.getChildren()) {
                if (sn.equals(necessaryNode)) {
                    parentNode = sn;
                }
            }
        }
        return parentNode;
    }

    public void addChild(SystemNode parent, SystemNode child) {
        parent.addChild(child);
        child.setParent(parent);
        nodeCount++;
    }
}


class SystemNode {

    protected int id;
    protected String name;
    private SystemNode parent;
    private Set<SystemNode> childSet;

    public SystemNode(String name, int id) {
        this.id = id;
        this.name = name;
        childSet = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                return node1.getId() - node2.getId();
            }
        });
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SystemNode getParent() {
        return parent;
    }

    public void setParent(SystemNode parent) {
        this.parent = parent;
    }

    public Set<SystemNode> getChildren() {
        return childSet;
    }

    public void addChild(SystemNode child) {
        childSet.add(child);
        child.setParent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SystemNode that = (SystemNode) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer line = new StringBuffer(MessageFormat.format("{0} {1}\n", this.getName(), this.getId()));
        for (SystemNode node : this.getChildren()) {
            line.append(toString(node));
        }
        return line.toString();
    }

    private StringBuffer toString(SystemNode highNode) {
        StringBuffer line = new StringBuffer(MessageFormat.format("{0} {1}", highNode.getId(), highNode.getName()));
        SystemNode parent = highNode.getParent();
        while (parent != null) {
            line.append(" <- ");
            line.append(parent.getName());
            parent = parent.getParent();
        }
        line.append('\n');
        for (SystemNode node : highNode.getChildren()) {
            line.append(toString(node));
        }
        return line;
    }
}


class ReaderFactory {

    public static AbstractReader createReader(String type, Reader reader) {
        FormatType readerType = FormatType.valueOf(type);

        AbstractReader necessaryReader;

        switch (readerType) {
            case find:
                necessaryReader = new FindReader(reader);
                break;
            case python:
                necessaryReader = new PythonReader(reader);
                break;
            case acm1:
                necessaryReader = new Acm1Reader(reader);
                break;
            case acm2:
                necessaryReader = new Acm2Reader(reader);
                break;
            case acm3:
                necessaryReader = new Acm3Reader(reader);
                break;
            case xml:
                necessaryReader = new XmlReader(reader);
                break;
            default:
                throw new IllegalArgumentException("Unsupported reader type: " + type);
        }
        return necessaryReader;
    }
}


class WriterFactory {

    public static AbstractWriter createWriter(String type, FileTree tree, PrintStream stream) {
        FormatType writerType = FormatType.valueOf(type);

        AbstractWriter necessaryWriter = null;
        switch (writerType) {
            case find:
                necessaryWriter = new FindWriter(tree, stream);
                break;
            case python:
                necessaryWriter = new PythonWriter(tree, stream);
                break;
            case acm1:
                necessaryWriter = new Acm1Writer(tree, stream);
                break;
            case acm2:
                necessaryWriter = new Acm2Writer(tree, stream);
                break;
            case acm3:
                necessaryWriter = new Acm3Writer(tree, stream);
                break;
            case xml:
                necessaryWriter = new XmlWriter(tree, stream);
                break;
            default:
                throw new IllegalArgumentException("Unsupported writer type: " + type);
        }
        return necessaryWriter;
    }
}


abstract class AbstractReader {

    private int nodeNumber = -1;
    protected BufferedReader reader;

    private int lineCounter;

    protected AbstractReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    protected String readLine() throws IOException {
        String line = null;
        if (lineCounter == 0) {
            line = reader.readLine();
            nodeNumber = Integer.parseInt(line);
            ++lineCounter;
        } else if (lineCounter <= nodeNumber) {
            line = reader.readLine();
            ++lineCounter;
        } else{
            reader.close();
        }
        return line;
    }

    public abstract FileTree read() throws IOException;

    protected int getNodesNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(String line) {
        this.nodeNumber = Integer.parseInt(line);
    }

    protected SystemNode createNode(String nodeString) {
        String[] nodeData = nodeString.split(" ");
        return new SystemNode(nodeData[0], Integer.parseInt(nodeData[1]));
    }
}



class AcmReader extends AbstractReader {

    protected Map<Integer, SystemNode> nodeMap = new LinkedHashMap<Integer, SystemNode>();

    private int lineCounter;
    private int linesNumber;

    protected AcmReader(Reader reader) {
        super(reader);
    }

    @Override
    protected String readLine() throws IOException {
        String line = null;
        if (lineCounter == 0) {
            line = reader.readLine();
            linesNumber = Integer.parseInt(line) * 2 + 1;
            ++lineCounter;
        } else if (lineCounter <= linesNumber) {
            line = reader.readLine();
            ++lineCounter;
        } else{
            reader.close();
        }
        return line;
    }

    @Override
    public FileTree read() throws IOException {
        return null;
    }

    protected void readNodes() throws IOException {
        for (int i = 1; i < getNodesNumber(); i++) {
            SystemNode node = createNode(readLine());
            nodeMap.put(node.getId(), node);
        }
    }
}


class Acm1Reader
        extends AcmReader {

    public Acm1Reader(Reader reader) {
        super(reader);
    }

    @Override
    public FileTree read() throws IOException {

        FileTree tree = new FileTree();
        setNodeNumber(readLine());

        SystemNode rootNode = createNode(readLine());
        tree.setRootNode(rootNode);

        nodeMap.put(rootNode.getId(), rootNode);

        growTree(tree);

        return tree;
    }


    private void growTree(FileTree tree) throws IOException {
        readNodes();
        connectNodes(tree);
    }


    private void connectNodes(FileTree tree) throws IOException {
        for (int key : nodeMap.keySet()) {
            String[] split = readLine().split(" ");
            SystemNode currentParent = nodeMap.get(key);
            for (int i = 1; i <= Integer.parseInt(split[0]); i++) {
                SystemNode currentChild = nodeMap.get(Integer.parseInt(split[i]));
                tree.addChild(currentParent, currentChild);
            }
        }
    }
}


class Acm2Reader
        extends AcmReader {

    public Acm2Reader(Reader reader) {
        super(reader);
    }

    @Override
    public FileTree read() throws IOException {
        setNodeNumber(readLine());
        FileTree tree = new FileTree();

        SystemNode rootNode = growTree(tree);
        tree.setRootNode(rootNode);

        return tree;
    }

    private SystemNode growTree(FileTree tree) throws IOException {
        readNodes();
        SystemNode rootNode = connectNodes(tree);
        return rootNode;
    }

    protected void readNodes() throws IOException {
        for (int i = 1; i <= getNodesNumber(); i++) {
            SystemNode node = createNode(readLine());
            nodeMap.put(node.getId(), node);
        }
    }

    private SystemNode connectNodes(FileTree tree) throws IOException {
        SystemNode rootNode = null;
        for (Map.Entry<Integer, SystemNode> entry : nodeMap.entrySet()) {
            int parentId = Integer.parseInt(readLine());
            if (parentId == -1) {
                rootNode = entry.getValue();
            } else {
                tree.addChild(nodeMap.get(parentId), entry.getValue());
            }
        }
        return rootNode;
    }
}


class Acm3Reader
        extends AcmReader {

    public Acm3Reader(Reader reader) {
        super(reader);
    }

    @Override
    public FileTree read() throws IOException {
        setNodeNumber(readLine());
        FileTree tree = new FileTree();

        SystemNode rootNode = growTree(tree);
        tree.setRootNode(rootNode);

        return tree;
    }

    private SystemNode growTree(FileTree tree) throws IOException {
        getNodesNumber();
        SystemNode rootNode = createNode(readLine());
        nodeMap.put(rootNode.getId(), rootNode);
        readNodes();
        connectNodes(tree);

        return rootNode;
    }

    private void connectNodes(FileTree tree) throws IOException {
        for (int i = 1; i < getNodesNumber(); i++) {
            String[] vertexId = readLine().split(" ");
            SystemNode parent = nodeMap.get(Integer.parseInt(vertexId[0]));
            SystemNode child = nodeMap.get(Integer.parseInt(vertexId[1]));
            tree.addChild(parent, child);
        }
    }
}


class FindReader extends AbstractReader {

    private String[] parents;

    public FindReader(Reader reader) {
        super(reader);
    }

    @Override
    public FileTree read() throws IOException {

        FileTree tree = new FileTree();

        setNodeNumber(readLine());
        SystemNode rootNode = parseNode(readLine());
        tree.setRootNode(rootNode);

        for (int lineNr = 2; lineNr <= getNodesNumber(); lineNr++) {
            SystemNode node = parseNode(readLine());
            tree.addChild(node, parents);
        }

        return tree;
    }

    private SystemNode parseNode(String line) {
        String[] pathAndId = line.split(" ");
        int id = Integer.parseInt(pathAndId[1]);

        String[] nodes = pathAndId[0].split("/");
        parents = null;
        String name;
        if (nodes.length == 1) {
            name = nodes[0];
        } else {
            name = nodes[nodes.length - 1];
            parents = Arrays.copyOf(nodes, nodes.length - 1);
        }
        return new SystemNode(name, id);
    }
}


class PythonReader extends AbstractReader {

    int level = 1;
    private Stack<SystemNode> nodeStack = new Stack<SystemNode>();

    public PythonReader(Reader reader) {
        super(reader);
    }

    @Override
    public FileTree read() throws IOException {

        FileTree tree = new FileTree();

        setNodeNumber(readLine());
        final SystemNode rootNode = createNode(readLine());
        tree.setRootNode(rootNode);
        nodeStack.add(rootNode);

        growTree(tree);

        return tree;
    }

    private void growTree(FileTree tree) throws IOException {
        SystemNode previousNode = null;
        for (int i = 2; i <= getNodesNumber(); i++) {
            String nodeLine = readLine();
            int nodeLevel = getNodeLevel(nodeLine);
            SystemNode node = parseNode(nodeLine);

            if (nodeLevel > level) {
                nodeStack.push(previousNode);
            } else if (nodeLevel < level) {
                for (int cnt = 0; cnt < level - nodeLevel; cnt++) {
                    nodeStack.pop();
                }
            }
            tree.addChild(node, nodeStack);
            previousNode = node;
            level = nodeLevel;
        }
    }

    private SystemNode parseNode(String nodeLine) {
        String trim = nodeLine.trim();
        SystemNode node = createNode(trim);
        return node;
    }

    private int getNodeLevel(String nodeLine) {
        int cnt = 0;
        while (nodeLine.charAt(cnt) == ' ') {
            ++cnt;
        }
        return cnt / 4;
    }
}


class XmlReader extends AbstractReader {

    private static final String NAME = "name";
    private static final String ID = "id";
    private Stack<SystemNode> nodeStack = new Stack<SystemNode>();
    private boolean isFirstTime = true;

    public XmlReader(Reader reader) {
        super(reader);
    }

    @Override
    protected String readLine() throws IOException {
        String line = null;
        if (!nodeStack.isEmpty() || isFirstTime) {
            line = reader.readLine();
            isFirstTime = false;
        } else{
            reader.close();
        }
        return line;
    }

    @Override
    public FileTree read() throws IOException {
        FileTree tree = new FileTree();
        SystemNode rootNode = parseDir(readLine());
        nodeStack.push(rootNode);
        tree.setRootNode(rootNode);
        growTree(tree);
        return tree;
    }

    private void growTree(FileTree tree) throws IOException {
        String line;
        while ((line = readLine()) != null) {
            if (isFile(line)) {
                SystemNode child = parseFile(line);
                tree.addChild(nodeStack.peek(), child);
            } else if (isEndDir(line)) {
                nodeStack.pop();
            } else {
                SystemNode child = parseDir(line);
                tree.addChild(nodeStack.peek(), child);
                nodeStack.push(child);
            }
        }
    }

    private boolean isFile(String line) {
        return line.contains("<file ");
    }

    private SystemNode parseFile(String line) {
        String propertiesOnly = line.substring(line.indexOf("<file") + 6, line.length() - 2).trim();
        Map<String, String> propertyMap = mapProperties(propertiesOnly);
        return new SystemNode(propertyMap.get(NAME), Integer.parseInt(propertyMap.get(ID)));
    }

    private boolean isEndDir(String line) {
        return line.trim().equals("</dir>");
    }

    private SystemNode parseDir(String line) {
        String propertiesOnly = line.substring(line.indexOf("<dir") + 5, line.length() - 1).trim();
        Map<String, String> propertyMap = mapProperties(propertiesOnly);
        return new SystemNode(propertyMap.get(NAME), Integer.parseInt(propertyMap.get(ID)));
    }

    private Map<String, String> mapProperties(String propertiesOnly) {
        String[] properties = propertiesOnly.split(" ");
        Map<String, String> propertyMap = new HashMap<String, String>();
        String[] property0 = properties[0].split("=");
        String[] property1 = properties[1].split("=");
        propertyMap.put(property0[0], property0[1].substring(1, property0[1].length() - 1));
        propertyMap.put(property1[0], property1[1].substring(1, property1[1].length() - 1));
        return propertyMap;
    }
}


abstract class AbstractWriter {

    protected final char SEPARATOR = ' ';
    protected FileTree tree;
    protected PrintStream printStream;

    public AbstractWriter(FileTree tree, PrintStream printStream) {
        this.tree = tree;
        this.printStream = printStream;
    }

    public abstract void write();
}


abstract class AcmWriter extends AbstractWriter {
    protected Set<SystemNode> nodes;

    protected AcmWriter(FileTree tree, PrintStream printStream) {
        super(tree, printStream);
        nodes = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                return node1.getId() - node2.getId();
            }
        });
    }

    @Override
    public void write() {
        printStream.println(tree.getNodeCount());
        readNodes(tree.getRootNode());
        printNodes();
        printIdentifiers();
        printStream.close();
    }

    protected void readNodes(SystemNode node) {
        nodes.add(node);
        for (SystemNode sn : node.getChildren()) {
            readNodes(sn);
        }
    }


    protected void printNodes() {
        for (SystemNode node : nodes) {
            printStream.println(new StringBuilder().append(node.getName()).append(SEPARATOR).append(node.getId()));
        }
    }

    protected abstract void printIdentifiers();
}


class Acm1Writer extends AcmWriter {

    public Acm1Writer(FileTree tree, PrintStream stream) {
        super(tree, stream);
        nodes = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                return node1.getId() - node2.getId();
            }
        });
    }

    @Override
    protected void printIdentifiers() {
        for (SystemNode node : nodes) {
            StringBuilder sb = new StringBuilder().append(node.getChildren().size());
            for (SystemNode child : node.getChildren()) {
                sb.append(SEPARATOR).append(child.getId());
            }
            printStream.println(sb);
        }
    }
}


class Acm2Writer extends AcmWriter {

    public Acm2Writer(FileTree tree, PrintStream printStream) {
        super(tree, printStream);
    }

    @Override
    protected void printIdentifiers() {
        for (SystemNode node : nodes) {
            if (node.getParent() == null) {
                printStream.println(-1);
            } else {
                printStream.println(node.getParent().getId());
            }
        }
    }
}


class Acm3Writer extends AcmWriter {

    public Acm3Writer(FileTree tree, PrintStream stream) {
        super(tree, stream);
    }

    @Override
    protected void printIdentifiers() {
        Set<SystemNode> properlySortedNodes = sortNodes();
        Iterator<SystemNode> iterator = properlySortedNodes.iterator();
        iterator.next();
        while (iterator.hasNext()) {
            SystemNode node = iterator.next();
            printStream.println(new StringBuilder().append(node.getParent().getId()).append(SEPARATOR).append(node.getId()));
        }
    }

    private Set<SystemNode> sortNodes() {
        Set<SystemNode> properlySortedNodes = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                int parent1id = node1.getParent() == null ? 0 : node1.getParent().getId();
                int parent2id = node2.getParent() == null ? 0 : node2.getParent().getId();
                int diff = parent1id - parent2id;
                return diff != 0 ? diff : node1.getId() - node2.getId();
            }
        });
        for (SystemNode node : nodes) {
            properlySortedNodes.add(node);
        }
        return properlySortedNodes;
    }
}


class FindWriter extends AbstractWriter {

    Set<SystemNode> nodes;

    public FindWriter(FileTree tree, PrintStream stream) {
        super(tree, stream);
        nodes = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                return node1.getId() - node2.getId();
            }
        });
    }

    @Override
    public void write() {
        printStream.println(tree.getNodeCount());
        dig(tree.getRootNode());
        printNodes();
    }

    private void printNodes() {
        for(SystemNode node : nodes){
            printNode(node, new Stack<String>(), node.getId());
        }
    }

    private void dig(SystemNode node) {
        Set<SystemNode> children = node.getChildren();
        nodes.add(node);
        for (SystemNode child : children) {
            dig(child);
        }
    }

    private void printNode(SystemNode node, Stack<String> children, int id) {
        if (node.getParent() == null) {
            StringBuilder builder = new StringBuilder(node.getName());
            while (!children.isEmpty()) {
                builder.append('/');
                builder.append(children.pop());
            }
            builder.append(SEPARATOR);
            builder.append(id);
            printStream.println(builder);
        } else {
            children.push(node.getName());
            printNode(node.getParent(), children, id);
        }
    }
}


class PythonWriter extends AbstractWriter {

    protected PythonWriter(FileTree tree, PrintStream stream) {
        super(tree, stream);
    }

    @Override
    public void write() {
        int level = 0;
        printStream.println(tree.getNodeCount());
        printNode(tree.getRootNode(), level);
        printStream.close();
    }

    private void printNode(SystemNode node, int level) {
        printStream.println(nodeToString(node, level));
        for (SystemNode child : node.getChildren()) {
            printNode(child, level + 1);
        }
    }

    private String nodeToString(SystemNode node, int level) {
        char[] data = new char[level * 4];
        Arrays.fill(data, SEPARATOR);
        StringBuilder sb = new StringBuilder();
        return sb.append(data).append(node.getName()).append(SEPARATOR).append(node.getId()).toString();
    }
}


class XmlWriter extends AbstractWriter {

    public static final String FILE_OPEN = "<file name='";
    public static final String ID = "' id='";
    public static final String FILE_CLOSE = "'/>";
    public static final String DIR_OPEN = "<dir name='";
    public static final String DIR_END = "'>";
    public static final String DIR_CLOSE = "</dir>";
    Set<SystemNode> sortedNodes;

    public XmlWriter(FileTree tree, PrintStream printStream) {
        super(tree, printStream);
        sortedNodes = new TreeSet<SystemNode>(new Comparator<SystemNode>() {
            @Override
            public int compare(SystemNode node1, SystemNode node2) {
                int parent1id = node1.getParent() == null ? 0 : node1.getParent().getId();
                int parent2id = node2.getParent() == null ? 0 : node2.getParent().getId();
                int diff = parent1id - parent2id;
                return diff != 0 ? diff : node1.getId() - node2.getId();
            }
        });
    }


    @Override
    public void write() {
        sortNodes(tree.getRootNode());
        int level = 0;
        printTree(tree.getRootNode(), level);
        printStream.close();
    }


    private void printTree(SystemNode node, int level) {
        if (node.getChildren().size() > 0) {
            printDirectory(node, level);
            for (SystemNode child : node.getChildren()) {
                printTree(child, level + 1);
            }
            closeDirectory(level);
        } else {
            printFolder(node, level);
        }
    }


    private void printDirectory(SystemNode node, int level) {
        char[] spaces = createIndent(level);
        StringBuilder sb = new StringBuilder().append(createIndent(level)).append(DIR_OPEN).append(node.getName()).append(ID)
                .append(node.getId()).append(DIR_END);
        printStream.println(sb.toString());
    }


    private void closeDirectory(int level) {
        printStream.println(new StringBuilder().append(createIndent(level)).append(DIR_CLOSE).toString());
    }


    private void printFolder(SystemNode node, int level) {
        StringBuilder sb = new StringBuilder().append(createIndent(level)).append(FILE_OPEN)
                .append(node.getName()).append(ID).append(node.getId()).append(FILE_CLOSE);
        printStream.println(sb.toString());
    }


    private char[] createIndent(int level) {
        char[] spaces = new char[level * 2];
        Arrays.fill(spaces, SEPARATOR);
        return spaces;
    }


    private void sortNodes(SystemNode node) {
        sortedNodes.add(node);
        for (SystemNode child : node.getChildren()) {
            sortNodes(child);
        }
    }
}

