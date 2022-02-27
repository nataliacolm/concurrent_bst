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
    // Atomic type MODE?

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
/*
    public boolean delete(int key)
    {
        // Injection mode: mark the leaf node that contains the given key
        // by flagging its incoming edge
        // Cleanup mode: remove leaf node that was flagges during injection.

       // mode = injection
       while (true)
       {
           seek(key); // updates seekRecord
           Node parent = this.seekRecord.parent;

           // address of child field

           if (mode == INJECTION)
           {
               Node terminal = this.seekRecord.terminal;
               if (terminal.getKey() != key)
               {    
                    // no key found.
                    return false;
               }   
           
                // determine where result should stand
                // result = CAS();
                // 

                // CAS instruction succeeds.
                if (result)
                {
                    mode = CLEANUP;
                    done = Cleanup();

                    if (done)
                    return true;
                }

                // CAS instruction fails.
                else
                {
                    // flag
                    if (address == terminal && flag || tag)
                    {
                        Cleanup();
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
                    done = cleanup();
                    if (done)
                    {
                        return true;
                    }
                }
            }
       }
    }
    */

    public static void main (String [] args)
    {
        // Set initialize the Seek Record

        /*
        bst = new Node(100);
        Node temp1 = new Node(90);
        Node temp2 = new Node(110);
        Node temp3 = new Node(95);
        Node temp4 = new Node(85);
        Node temp5 = new Node(88);
        Node temp6 = new Node(70);

        bst.left = new AtomicStampedReference<>(temp1, 0);
        bst.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp4, 0);
        temp1.left = new AtomicStampedReference<>(temp3, 0);
        temp4.left = new AtomicStampedReference<>(temp6, 0);
        temp4.right = new AtomicStampedReference<>(temp5, 0);

        seekRecord = new SeekRecord(bst, temp1, temp1, temp4);

        seek(88);

*/

    }
}
