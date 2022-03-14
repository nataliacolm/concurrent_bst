import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicStampedReference;

// import org.w3c.dom.Node;

// For testing purposes, key is of type int.

class Node {
    // Stamp:
    // 0 = unflagged & untagged
    // 10 = flagged & untagged
    // 1 = unflagged & tagged
    // 11 = flagged & tagged

    private int key;
    AtomicStampedReference<Node> left = new AtomicStampedReference<>(null, 0);
    AtomicStampedReference<Node> right = new AtomicStampedReference<>(null, 0);

    Node(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}

class SeekRecord {
    public Node ancestor;
    public Node succesor;
    public Node parent;
    public Node terminal;

    SeekRecord(Node ancestor, Node succesor, Node parent, Node terminal) {
        this.ancestor = ancestor;
        this.succesor = succesor;
        this.parent = parent;
        this.terminal = terminal;
    }
}

public class ConcurrentBST {
    public Node root;
    private final int INJECTION = 1;
    private final int CLEANUP = 0;

    public AtomicStampedReference<Node> getNextChildField(int key, Node current) {
        if (current.getKey() > key) {
            return current.left;
        } else {
            return current.right;
        }
    }

    // Watch out: overloaded method
    public Node getAddressOfNextChildField(int key, Node child) {
        if (child.getKey() > key) {
            return child.left.getReference();
        } else {
            return child.right.getReference();
        }
    }

    // Watch out: overloaded method.
    public Node getAddressOfNextChildField(Node node, Node child) {
        if (child.getKey() < node.getKey()) {
            return node.left.getReference();
        } else {
            return node.right.getReference();
        }
    }

    // Return address of child field that contains address of the sibling of the
    // next node on the access path
    public Node getAddressOfSiblingChildField(Node node, Node child) {
        if (child.getKey() < node.getKey()) {
            return node.right.getReference();
        } else {
            return node.left.getReference();
        }
    }

    public SeekRecord seek(int key) {
        Node ancestor = root;
        Node succesor = root.left.getReference();
        Node parent = root.left.getReference();
        Node current = parent.left.getReference();

        AtomicStampedReference<Node> childFieldAtParent = parent.left;
        AtomicStampedReference<Node> childFieldAtCurrent = current.left;
        Node next = childFieldAtParent.getReference();

        while (next != null) {
            if (childFieldAtParent.getStamp() == 00 || childFieldAtParent.getStamp() == 10) {
                ancestor = parent;
                succesor = current;
            }

            parent = current;
            current = next;

            childFieldAtParent = childFieldAtCurrent;
            childFieldAtCurrent = getNextChildField(key, current);

            if (childFieldAtCurrent == null) {
                next = null;
            } else {
                next = childFieldAtCurrent.getReference();
            }
        }

        return new SeekRecord(ancestor, succesor, parent, current);
    }

    public boolean delete(int key) {
        // Injection mode: mark the leaf node that contains the given key
        // by flagging its incoming edge
        // Cleanup mode: remove leaf node that was flagges during injection.

        int mode = INJECTION;
        while (true) {
            SeekRecord seekRecord = seek(key); // updates seekRecord
            Node parent = seekRecord.parent;
            Node terminal = null;

            // Test section
            AtomicStampedReference<Node> addressOfChildField = getNextChildField(key, parent);
            // End of test section
            // address of child field

            if (mode == INJECTION) {
                terminal = seekRecord.terminal;
                if (terminal.getKey() != key) {
                    // no key found.
                    return false;
                }

                // determine where result should stand
                boolean result = addressOfChildField.compareAndSet(terminal, terminal, 0, 10);

                // CAS instruction succeeds.
                if (result) {
                    mode = CLEANUP;
                    // TODO
                    // done = Cleanup();
                    // dummy hold until cleanup is complete
                    boolean done = true;

                    if (done)
                        return true;
                }

                // CAS instruction fails.
                else {
                    int[] stamp = new int[1];
                    // flag
                    Node address = addressOfChildField.get(stamp);
                    // All possible flag or tags
                    if (address == terminal && (stamp[0] == 10 || stamp[0] == 1 || stamp[0] == 11)) {
                        // TODO
                        // Cleanup();
                    }
                }

            }

            else {
                if (seekRecord.terminal != terminal) {
                    return true;
                }

                else {
                    // TODO
                    // done = cleanup();

                    // dummy hold until cleanup is complete.
                    boolean done = true;
                    if (done) {
                        return true;
                    }
                }
            }
        }
    }

    public boolean insert(int key) {
        while (true) {
            SeekRecord seekRecord = seek(key);
            if (seekRecord.terminal.getKey() != key) {
                Node parent = seekRecord.parent;
                Node terminal = seekRecord.terminal;

                Node addressOfChildField = getAddressOfNextChildField(key, parent);

                // create two nodes newInternal and newLeaf and initialize them appropriately
                Node newInternal = new Node(key);

                // initialize to false
                boolean result = false;

                if (addressOfChildField.getKey() == terminal.getKey())
                {
                    result = true;

                    if (addressOfChildField.getKey() > key)
                    {
                        addressOfChildField.left = new AtomicStampedReference<Node>(newInternal, 0);
                    }

                    else
                    {
                        addressOfChildField.right = new AtomicStampedReference<Node>(newInternal, 0);
                    }
                }
                
                if (result) {
                    return true;
                } else {
                    AtomicStampedReference<Node> child = getNextChildField(key, parent);
                    int stamp = child.getStamp();
                    // flag
                    Node address = child.getReference();
                    if (address == terminal && (stamp == 10 || stamp == 1 || stamp == 11)) {
                        // TODO
                        // Cleanup();
                    }
                }
            } else {
                return false;
            }
        }

    }

    // Removes a leaf node, which is currently under deletion, and its parent from
    // the tree
    public boolean cleanup(int key) {

        SeekRecord seekRecord = seek(key);

        // retrieve all addresses in the seekRecord for easy access
        Node ancestor = seekRecord.ancestor;
        Node succesor = seekRecord.succesor;
        Node parent = seekRecord.parent;
        Node terminal = seekRecord.terminal;

        // obtain the addresses on which atomic instructions will be executed
        // first obtain the address of the field of the ancestor node that will be
        // modified
        Node addressOfSuccessorField = getAddressOfNextChildField(ancestor, succesor);

        // retrieve the address of the children fields of the parent node
        Node addressOfChildField = getAddressOfNextChildField(parent, terminal);
        Node addressOfSiblingField = getAddressOfSiblingChildField(parent, terminal);

        // create the stamp
        int[] stamp = new int[1];

        // if not flag then the leaf node is not flagged for deletion
        if (stamp[0] == 1 || stamp[0] == 1) {
            // The leaf node is not flagged for deletion, thus the siblign node must be
            // flagged for deletion
            // switch the sibling address
            addressOfSiblingField = addressOfChildField;

        }
        // end of if

        /*
         * Problem*
         * The next step is the Freeze step which you tag the sibling edge if its not
         * already tagged
         * there cannot be any modifying operation at this point on the edge
         *
         * In here if the stamps are untagged then we do a Bit test and set instruction.
         * Instead of doing this BTS we can simulate it doing a CAS instruction.
         *
         * after this we get the result which before we will need to get the address of
         * the sibling field and the flag
         * I did a new helper function to get the address of the sibling field which is
         * described in the document
         * but when attempting to get the address with this helper function with the
         * stamp, is not possible since the helper function is not in
         * AtomicStampedReference
         * Cant wrap my head around this
         * refer to page 14 for more information on this part.
         */

        // if not tagged then CAS instruction
        if (stamp[0] == 10 || stamp[0] != 0) {

            // speficic CAS instruction goes here
            // repeatedly readthe contents stored in the right field of the parent and then
            // attempt to set the tag bit in right field of parent to 1.

        }

        // get the address of the sibling field
        //Node address = addressOfSiblingField.get(stamp);

        // the flag field will be copied to the new edge that will be created
        // prune step: make the sibling node a direct child of the ancestor node
        // boolean result = addressOfSuccessorField.compareAndSet(succesor, parent, 0,
        // 1);

        // dummy hold until prune is complete
        boolean done = true;
        if (done == true) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        // Set initialize the Seek Record
        ConcurrentBST bst = new ConcurrentBST();
        bst.root = new Node(100);
        Node temp1 = new Node(90);
        Node temp2 = new Node(110);
        Node temp3 = new Node(95);
        Node temp4 = new Node(85);
        Node temp5 = new Node(88);
        Node temp6 = new Node(70);
        Node temp7 = new Node(87);
        Node temp8 = new Node(89);
        Node temp9 = new Node(75);
        Node temp10 = new Node(65);

        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp4, 0);
        temp1.right = new AtomicStampedReference<>(temp3, 0);
        temp4.left = new AtomicStampedReference<>(temp6, 0);
        temp4.right = new AtomicStampedReference<>(temp5, 0);
        temp5.left = new AtomicStampedReference<>(temp7, 0);
        temp5.right = new AtomicStampedReference<>(temp8, 0);
        temp6.left = new AtomicStampedReference<>(temp10, 0);
        temp6.right = new AtomicStampedReference<>(temp9, 0);

        SeekRecord record = bst.seek(65);

        System.out.println(record.terminal.getKey());

        boolean gotInserted = bst.insert(55);
        boolean gotInserted2 = bst.insert(68);

        System.out.println(gotInserted);
        System.out.println(gotInserted2);

        record = bst.seek(55);
        System.out.println(record.parent.getKey());
        System.out.println(record.parent.left.getReference().getKey());

        record = bst.seek(68);
        System.out.println(record.parent.getKey());
        System.out.println(record.parent.right.getReference().getKey());

    }
}
