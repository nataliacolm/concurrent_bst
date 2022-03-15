import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicStampedReference;

// import org.w3c.dom.Node;

// For testing purposes, key is of type int.

class Node
{
    // Stamp:
    // 0 = unflagged & untagged
    // 10 = flagged & untagged
    // 1 = unflagged & tagged
    // 11 = flagged & tagged

    private int key;
    AtomicStampedReference<Node> left = new AtomicStampedReference<>(null, 0);
    AtomicStampedReference<Node> right = new AtomicStampedReference<>(null, 0);

    Node(int key)
    {
        this.key = key;
    }

    public int getKey()
    {
        return this.key;
    }
}

class SeekRecord
{
    public Node ancestor;
    public Node successor;
    public Node parent;
    public Node terminal;

    SeekRecord(Node ancestor, Node successor, Node parent, Node terminal)
    {
        this.ancestor = ancestor;
        this.successor = successor;
        this.parent = parent;
        this.terminal = terminal;
    }
}

public class ConcurrentBST
{
    public Node root;
    private final int INJECTION = 1;
    private final int CLEANUP = 0;

    public AtomicStampedReference<Node> getNextChildField(int key, Node current)
    {
        if (current.getKey() > key)
        {
            return current.left;
        }

        else
        {
            return current.right;
        }
    }

    // Watch out: overloaded method
    public Node getAddressOfNextChildField(int key, Node child)
    {
        if (child.getKey() > key)
        {
            return child.left.getReference();
        }

        else
        {
            return child.right.getReference();
        }
    }

    // Watch out: overloaded method.
    public AtomicStampedReference<Node> getAddressOfNextChildField(Node node, Node child)
    {
        if (child.getKey() < node.getKey())
        {
            return node.left;
        }
        else
        {
            return node.right;
        }
    }

    // Return address of child field that contains address of the sibling of the
    // next node on the access path
    public AtomicStampedReference<Node> getAddressOfSiblingChildField(Node node, Node child)
    {
        if (child.getKey() < node.getKey())
        {
            return node.right;
        }
        else
        {
            return node.left;
        }
    }

    public SeekRecord seek(int key)
    {
        Node ancestor = root;
        Node successor = root.left.getReference();
        Node parent = root.left.getReference();
        Node current = parent.left.getReference();

        AtomicStampedReference<Node> childFieldAtParent = parent.left;
        AtomicStampedReference<Node> childFieldAtCurrent = current.left;
        Node next = childFieldAtCurrent.getReference();

        while (next != null)
        {
            if (childFieldAtParent.getStamp() == 00 || childFieldAtParent.getStamp() == 10)
            {
                ancestor = parent;
                successor = current;
            }

            parent = current;
            current = next;

            childFieldAtParent = childFieldAtCurrent;
            childFieldAtCurrent = getNextChildField(key, current);

            if (childFieldAtCurrent.getReference() == null)
            {
                next = null;
            }

            else {
                next = childFieldAtCurrent.getReference();
            }
        }

        return new SeekRecord(ancestor, successor, parent, current);
    }

    public boolean delete(int key)
    {
        // Injection mode: mark the leaf node that contains the given key
        // by flagging its incoming edge
        // Cleanup mode: remove leaf node that was flagges during injection.

        int mode = INJECTION;
        while (true)
        {
            SeekRecord seekRecord = seek(key); // updates seekRecord
            Node parent = seekRecord.parent;
            Node terminal = null;

            // Test section
            AtomicStampedReference<Node> addressOfChildField = getNextChildField(key, parent);
            // End of test section
            // address of child field

            if (mode == INJECTION)
            {
                terminal = seekRecord.terminal;
                if (terminal.getKey() != key)
                {
                    // no key found.
                    return false;
                }

                // determine where result should stand
                boolean result = addressOfChildField.compareAndSet(terminal, terminal, 0, 10);

                // CAS instruction succeeds.
                if (result)
                {
                    mode = CLEANUP;
                    // TODO
                    boolean done = cleanup(seekRecord);
                    // dummy hold until cleanup is complete

                    if (done)
                        return true;
                }

                // CAS instruction fails.
                else
                {
                    int[] stamp = new int[1];
                    // flag
                    Node address = addressOfChildField.get(stamp);
                    // All possible flag or tags
                    if (address == terminal && (stamp[0] == 10 || stamp[0] == 1 || stamp[0] == 11))
                    {
                        // TODO
                        boolean temp = cleanup(seekRecord);
                    }
                }

            }

            else
            {
                if (seekRecord.terminal != terminal)
                {
                    return true;
                }

                else
                {
                    boolean done = cleanup(seekRecord);

                    // dummy hold until cleanup is complete.
                    if (done)
                    {
                        return true;
                    }
                }
            }
        }
    }

    public boolean search(int key)
    {
        SeekRecord seekRecord = seek(key);
        if (seekRecord.terminal.getKey() == key)
            return true;
        return false;
    }

    public boolean insert(int key)
    {
        while (true)
        {
            SeekRecord seekRecord = seek(key);
            if (seekRecord.terminal.getKey() != key)
            {
                Node parent = seekRecord.parent;
                Node terminal = seekRecord.terminal;

                AtomicStampedReference<Node> addressOfChildField = getNextChildField(key, parent);

                // create two nodes newInternal and newLeaf and initialize them appropriately
                Node newInternal = new Node(Math.max(terminal.getKey(), key));
                Node newLeaf = new Node(key);

                if (key > terminal.getKey())
                {
                    newInternal.right = new AtomicStampedReference<Node>(newLeaf, 0);
                    newInternal.left = new AtomicStampedReference<Node>(terminal, 0);
                }

                else
                {
                    newInternal.right = new AtomicStampedReference<Node>(terminal, 0);
                    newInternal.left = new AtomicStampedReference<Node>(newLeaf, 0);
                }

                // initialize to false
                boolean result = addressOfChildField.compareAndSet(terminal, newInternal, 0, 0);

                if (result)
                {
                    return true;
                }

                else
                {
                    AtomicStampedReference<Node> child = getNextChildField(key, parent);
                    int stamp = child.getStamp();
                    // flag
                    Node address = child.getReference();
                    if (address == terminal && (stamp == 10 || stamp == 1 || stamp == 11))
                    {
                        boolean temp = cleanup(seekRecord);
                    }
                }
            }
            else
            {
                return false;
            }
        }

    }

    // Removes a leaf node, which is currently under deletion, and its parent from
    // the tree
    public boolean cleanup(SeekRecord seekRecord)
    {
        // retrieve all addresses in the seekRecord for easy access
        Node ancestor = seekRecord.ancestor;
        Node successor = seekRecord.successor;
        Node parent = seekRecord.parent;
        Node terminal = seekRecord.terminal;

        // obtain the addresses on which atomic instructions will be executed
        // first obtain the address of the field of the ancestor node that will be
        // modified
        AtomicStampedReference<Node> addressOfSuccessorField = getAddressOfNextChildField(ancestor, successor);

        // retrieve the address of the children fields of the parent node
        AtomicStampedReference<Node> addressOfChildField = getAddressOfNextChildField(parent, terminal);
        AtomicStampedReference<Node> addressOfSiblingField = getAddressOfSiblingChildField(parent, terminal);

        // create the stamp
        int[] stamp = new int[1];

        // if not flag then the leaf node is not flagged for deletion
        if (stamp[0] == 1 || stamp[0] == 0)
        {
            addressOfSiblingField = addressOfChildField;
        }
        // end of if

        // if not tagged then CAS instruction
        if (stamp[0] == 10 || stamp[0] == 0)
        {
            boolean temp = false;
            // speficic CAS instruction goes here
            // repeatedly readthe contents stored in the right field of the parent and then
            // attempt to set the tag bit in right field of parent to 1.
            if (stamp[0] == 10)
                temp = addressOfSiblingField.attemptStamp(addressOfSiblingField.getReference(), 11);
            else if (stamp[0] == 0)
                temp = addressOfSiblingField.attemptStamp(addressOfSiblingField.getReference(),1);
        }

        int[] stamp2 = new int[1];
        // get the address of the sibling field
        Node address2 = addressOfSiblingField.get(stamp2);

        // set mark to flag & untagged = 10
        boolean result = addressOfSuccessorField.compareAndSet(successor, address2, 0, 10);

        return result;
    }

    public static void main(String[] args)
    {
        // Set initialize the Seek Record
        ConcurrentBST bst = new ConcurrentBST();
        bst.root = new Node(100);
        Node temp1 = new Node(90);
        Node temp2 = new Node(110);
        Node temp3 = new Node(95);
        Node temp4 = new Node(85);

            // 100
        // 90      110
    // 85     95
  // 70  85


        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp4, 0);
        temp1.right = new AtomicStampedReference<>(temp3, 0);

        System.out.println(bst.root.left.getReference().getKey());
        System.out.println(bst.root.left.getReference().left.getReference().getKey());

        System.out.println("_____ Insert 70 _____");

        boolean didItInsert = bst.insert(70);

        System.out.println(didItInsert);
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().getKey() + " On the Left");
        System.out.println(bst.root.left.getReference().left.getReference().right.getReference().getKey() + " On the Right");

        System.out.println("_____ Insert 60 _____");

        boolean didItInsert2 = bst.insert(60);

        System.out.println(didItInsert2);
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().left.getReference().getKey() + " On the Left");
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().getKey() + " On the Right");

        System.out.println("_____ Insert 75 _____");

        boolean didItInsert3 = bst.insert(75);

        System.out.println(didItInsert3);
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().getKey() + " On Main");
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().left.getReference().getKey() + " On the Left");
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().right.getReference().getKey() + " On the Right");


        System.out.println("_____ Delete 60 _____");

        boolean didItDelete4 = bst.delete(60);

        System.out.println(didItDelete);
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().getKey() + " On Main");
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().left.getReference().getKey() + " On the Left");
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().getKey() + " On the Right");

    }
}
