package cnuphys.cnf.alldata.graphics;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

public class DragDropList extends JList {
  DefaultListModel model;

	public DragDropList(DefaultListModel model) {
		super(model);
		this.model = model;

		setDragEnabled(true);
		setDropMode(DropMode.INSERT);

		setTransferHandler(new MyListDropHandler(this));

		new MyDragListener(this);
	}
	

  public DragDropList() {
    super(new DefaultListModel());
    model = (DefaultListModel) getModel();
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);

    setTransferHandler(new MyListDropHandler(this));

    new MyDragListener(this);
    
    model.addElement("a");
    model.addElement("b");
    model.addElement("c");
  }

  public static void main(String[] a){
    JFrame f = new JFrame();
    f.add(new JScrollPane(new DragDropList()));
    f.setSize(300,300);
    f.setVisible(true);
  }
}

class MyDragListener implements DragSourceListener, DragGestureListener {
  DragDropList list;

  DragSource ds = new DragSource();

  public MyDragListener(DragDropList list) {
    this.list = list;
    DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(list,
        DnDConstants.ACTION_MOVE, this);

  }

  public void dragGestureRecognized(DragGestureEvent dge) {
    StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
    ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
  }

  public void dragEnter(DragSourceDragEvent dsde) {
  }

  public void dragExit(DragSourceEvent dse) {
  }

  public void dragOver(DragSourceDragEvent dsde) {
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
  }
}

class MyListDropHandler extends TransferHandler {
  DragDropList list;

  public MyListDropHandler(DragDropList list) {
    this.list = list;
  }

  public boolean canImport(TransferHandler.TransferSupport support) {
    if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return false;
    }
    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    if (dl.getIndex() == -1) {
      return false;
    } else {
      return true;
    }
  }

  public boolean importData(TransferHandler.TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }
    
    if (!(support.getComponent() instanceof DragDropList)) {
    	return false;
    }
    DragDropList list = (DragDropList)(support.getComponent());
    DefaultListModel model = list.model;
   

    Transferable transferable = support.getTransferable();
    String indexString;
    try {
      indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
    } catch (Exception e) {
      return false;
    }

    int index = Integer.parseInt(indexString);
    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    int dropIndex = dl.getIndex() - 1;

    dropIndex = Math.max(1,  Math.min(model.size(), dropIndex));
    
    
//    System.out.println("source Index: " + index);
//    System.out.println("  drop Index: " + dropIndex);
//    System.out.println("inserted");
    
		if (index != dropIndex) {
			int count = model.size();
			Object o1 = model.elementAt(index);
	//		System.err.println("object type: " + o1.getClass().getName());
	//		String s1 = (String) model.elementAt(index);
			model.removeElementAt(index);
			model.insertElementAt(o1, dropIndex);
		}
    
    return true;
  }
}