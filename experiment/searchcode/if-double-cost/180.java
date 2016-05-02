package net.asmcbain.swing.example.components.treetable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import net.asmcbain.swing.SwingUtil;
import net.asmcbain.swing.components.table.ConvenientJTable;
import net.asmcbain.swing.components.treetable.DefaultTreeTableModel;
import net.asmcbain.swing.components.treetable.TreeTableCellRenderer;
import net.asmcbain.swing.components.treetable.MutableTreeTableModel;
import net.asmcbain.swing.components.treetable.TreeTableNode;
import net.asmcbain.swing.example.AggregateExample;
import net.asmcbain.swing.example.Example;
import net.asmcbain.swing.example.ExampleUtility;

/**
 * An example that demonstrates classes from the treetable package.
 * 
 * @author Art McBain
 *
 */
@AggregateExample
public class TreeTableExample implements Example {

	public static void main(String[] args) {
		SwingUtil.setSystemLookAndFeel();

		ExampleUtility.launchExample("TreeTable Example",
		          createTreeTable(), 485, 300);
	}

	public static JPanel createTreeTable() {

		final MutableTreeTableModel model = new DefaultTreeTableModel();
		model.setColumnNames("Name", "Qty", "Cost");
		model.addNode(
			new Order("Order #3124", Arrays.asList(
					new Item("Widget", 3.75, 2),
					new Item("Thingamajig", 1.50, 5)
			))
		);
		model.addNode(
			new Order("Order #5241", Arrays.asList(
					new Item("Thingamabob", 9.25, 1),
					new Item("Doodad", 1.00, 12),
					new Item("Gadget", 5.00, 3)
			))
		);

		final ConvenientJTable table = new ConvenientJTable((TableModel)model);
		table.setIntercellSpacing(new Dimension(1, 1));
		table.setPreferredColumnWidths(.70, .15, .15);

		// Use the renderer for all columns
		// This renderer is pretty basic. It's probably better to use its code as a
		// base for a more suitable renderer (the code is pretty simple).  
		final TreeTableCellRenderer renderer = new TreeTableCellRenderer(model, true);

		table.getColumn(table.getColumnName(0)).setCellRenderer(renderer);
		table.getColumn(table.getColumnName(2)).setCellRenderer(renderer);

		// The TreeTable model works with non-"TreeTable" cell renderers
		table.getColumn(table.getColumnName(1)).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

				// Right align column 2 (numbers)
				setHorizontalAlignment(JLabel.RIGHT);
				return super.getTableCellRendererComponent(table, value + " ", isSelected, false, row, column);
			}
		});

		// Handle opening and closing of rows/nodes via double click
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TreeTableNode<?> node = model.getNodeAtRow(e.getY() / table.getRowHeight());

				if(e.getClickCount() == 2 && node instanceof Order) {
					if(model.isOpen(node)) {
						model.closeNode(node);
					} else {
						model.openNode(node);
					}
				}
			}
		});
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(BorderFactory.createEmptyBorder());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(pane, BorderLayout.CENTER);
		panel.add(new JLabel("Double click an order to show or hide the items listed on the order"), BorderLayout.SOUTH);
		return panel;
	}


	/*
	 *  TreeTable node classes.
	 *  This example doesn't use the parent property, but it could be useful in expanded
	 *  situations where it is necessary to walk up the tree from a child to a parent.
	 */

	private static class Order extends TreeTableNode<Item> {

		private final String name;
		private final List<Item> items;

		public Order(String name, List<Item> items) {
			super(null);
			this.name = name;
			this.items = items;
		}

		public List<? extends Item> getChildren() {
			return items;
		}

		// This should be returned as the first argument, and toString returning the necessary value.
		// The TreeTableModel relies on this. For an example of extending the model to change this, see below.
		public Object getColumnValue(int column) {
			return (column == 0)? this : (column == 1)? " " + items.size() : " $" + getTotalCost();
		}

		public Object getType() {
			return "order";
		}

		private double getTotalCost() {
			double cost = 0;

			for(Item item : items) {
				cost += item.getCost() * item.getQuantity();
			}

			return cost;
		}

		public String toString() {
			return " " + name;
		}

	}

	private static class Item extends  TreeTableNode<Item> {

		private final String name;
		private final double cost;
		private final int quantity;

		public Item(String name, double cost, int quantity) {
			super(null);
			this.name = name;
			this.cost = cost;
			this.quantity = quantity;
		}

		public List<? extends Item> getChildren() {
			return null;
		}

		public Object getColumnValue(int column) {
			return (column == 0)? this : (column == 1)? " " + quantity : " $" + cost;
		}

		public Object getType() {
			return "item";
		}

		public String toString() {
			return " " + name;
		}

		public double getCost() {
			return cost;
		}

		public int getQuantity() {
			return quantity;
		}

	}

}

