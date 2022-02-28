import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicStampedReference;

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

    Node (int key)
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
    public Node succesor;
    public Node parent;
    public Node terminal;

    SeekRecord (Node ancestor, Node succesor, Node parent, Node terminal)
    {
        this.ancestor = ancestor;
        this.succesor = succesor;
        this.parent = parent;
        this.terminal = terminal;
    }
}

public class ConcurrentBST
{
    public Node root;
    public SeekRecord seekRecord;
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
    public Node getAddressOfNextChildField(Node node, Node child)
    {
        if (child.getKey() < node.getKey())
        {
            return node.left.getReference();
        }
        else
        {
            return node.right.getReference();
        }
    }


    public void seek(int key)
    {
        Node ancestor = root;
        Node succesor = root.left.getReference();
        Node parent = root.left.getReference();
        Node current = parent.left.getReference();

        AtomicStampedReference<Node> childFieldAtParent = parent.left;
        AtomicStampedReference<Node> childFieldAtCurrent = current.left;
        Node next = childFieldAtParent.getReference();

        while (next != null)
        {
            if (childFieldAtParent.getStamp() == 00 || childFieldAtParent.getStamp() == 10)
            {
                ancestor = parent;
                succesor = current;
            }

            parent = current;
            current = next;

            childFieldAtParent = childFieldAtCurrent;
            childFieldAtCurrent = getNextChildField(key, current);

            if (childFieldAtCurrent == null)
            {
                next = null;
            }
            else
            {
                next = childFieldAtCurrent.getReference();
            }
        }

        seekRecord.ancestor = ancestor;
        seekRecord.succesor = succesor;
        seekRecord.parent = parent;
        seekRecord.terminal = current;
        return;
    }

    public boolean delete(int key)
    {
        // Injection mode: mark the leaf node that contains the given key
        // by flagging its incoming edge
        // Cleanup mode: remove leaf node that was flagges during injection.

       int mode = INJECTION;
       while (true)
       {
           seek(key); // updates seekRecord
           Node parent = this.seekRecord.parent;
           Node terminal = null;


            // Test section
            AtomicStampedReference<Node> addressOfChildField = getNextChildField(key, parent);
           // End of test section
           // address of child field

           if (mode == INJECTION)
           {
               terminal = this.seekRecord.terminal;
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
                    // done = Cleanup();
                    // dummy hold until cleanup is complete
                    boolean done = true;

                    if (done)
                        return true;
                }

                // CAS instruction fails.
                else
                {
                    int [] stamp = new int [1];
                    // flag
                    Node address = addressOfChildField.get(stamp);
                    // All possible flag or tags
                    if (address == terminal && (stamp[0] == 10 || stamp[0] == 1 || stamp[0] == 11))
                    {
                        // TODO
                        // Cleanup();
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
                    // TODO
                    // done = cleanup();

                    // dummy hold until cleanup is complete.
                    boolean done = true;
                    if (done)
                    {
                        return true;
                    }
                }
            }
       }
    }

    public static void main (String [] args)
    {
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

        bst.seekRecord = new SeekRecord(bst.root, temp1, temp1, temp4);

        bst.seek(65);


        System.out.println(bst.seekRecord.terminal.getKey());

    }
}
