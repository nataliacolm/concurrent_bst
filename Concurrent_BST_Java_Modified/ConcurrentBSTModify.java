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

class Stack
{
    public Node key;
    public Stack next;

    Stack(Node key)
    {
        this.key = key;
        next = null;
    }
}

class StackMethods
{
    Stack head;

    public void push(Node node)
    {
        Stack temp = head;
        Stack add = new Stack(node);

        head = add;
        add.next = temp;
    }

    public Node pop()
    {
        Stack temp = head;
        head = head.next;

        return temp.key;
    }

    public Node getHead()
    {
        return head.key;
    }

    public Node getSecondToTop()
    {
        return head.next.key;
    }

    public void initializeTraversalStack(Node root)
    {
        head = new Stack(root);
        head.next = new Stack(root.left.getReference());
    }
}

public class ConcurrentBSTModify
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

    public SeekRecord seek(int key, StackMethods stack)
    {
        while (true)
        {
            Node parent = stack.getHead();

            AtomicStampedReference<Node> childFieldAtParent = getNextChildField(key, parent);

            if (childFieldAtParent.getStamp() == 00)
            {
                Node current = childFieldAtParent.getReference();

                AtomicStampedReference<Node> childFieldAtCurrent = getNextChildField(key, current);

                if (childFieldAtCurrent.getReference() == null)
                {
                   return new SeekRecord(stack.getSecondToTop(), parent, parent, current);
                }

                else if (childFieldAtCurrent.getStamp() == 00)
                {
                    stack.push(current);
                }

                else
                {
                    SeekRecord record = new SeekRecord(parent, current, current, childFieldAtCurrent.getReference());
                    boolean didItClean = cleanup(record);
                }
            } // End of If

            else
            {
                if (stack.getHead().getKey() < 90000)
                {
                    Node node = stack.pop();
                }
            }
        } // End of While
    }

    public boolean delete(int key)
    {
        // Injection mode: mark the leaf node that contains the given key
        // by flagging its incoming edge
        // Cleanup mode: remove leaf node that was flagges during injection.

        StackMethods stack = new StackMethods();
        stack.initializeTraversalStack(root);

        int mode = INJECTION;
        while (true)
        {
            SeekRecord seekRecord = seek(key, stack); // updates seekRecord
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
        StackMethods stack = new StackMethods();
        stack.initializeTraversalStack(root);

        SeekRecord seekRecord = seek(key,stack);

        if (seekRecord.terminal.getKey() == key)
            return true;
        return false;
    }

    public boolean insert(int key)
    {
        StackMethods stack = new StackMethods();
        stack.initializeTraversalStack(root);

        while (true)
        {
            SeekRecord seekRecord = seek(key, stack);
            if (seekRecord.terminal.getKey() != key)
            {
                Node parent = seekRecord.parent;
                Node terminal = seekRecord.terminal;

                AtomicStampedReference<Node> addressOfChildField = getNextChildField(key, parent);

                // create two nodes newInternal and newLeaf and initialize them appropriately
                Node newInternal = new Node(Math.max(terminal.getKey(), key));
                Node newLeaf = new Node(key);
                Node newSibling = new Node(terminal.getKey());

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
        stamp[0] = addressOfChildField.getStamp();

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
        Node address2 = addressOfSiblingField.getReference();

        // set mark to flag & untagged = 10

        //boolean result = addressOfSuccessorField.compareAndSet(successor, address2, 0, 10);
        boolean result = addressOfSuccessorField.compareAndSet(successor, address2, 0, stamp2[0]);

        return result;
    }

    public static void main(String[] args)
    {
        // Set initialize the Seek Record
        ConcurrentBSTModify bst = new ConcurrentBSTModify();
        StackMethods stack = new StackMethods();
        bst.root = new Node(100);
        Node temp1 = new Node(90);
        Node temp2 = new Node(110);
        Node temp3 = new Node(95);
        Node temp4 = new Node(85);
        Node temp5 = new Node(84);
        Node temp6 = new Node(85);

              // 100
          // 90      110
      // 85     95
    //70    85
  //60  75
      //70  75

        // 75
    //70       75

        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp4, 0);
        temp1.right = new AtomicStampedReference<>(temp3, 0);
        temp4.left = new AtomicStampedReference<>(temp5, 0);
        temp4.right = new AtomicStampedReference<>(temp6, 0);

        stack.initializeTraversalStack(bst.root);

        SeekRecord record = bst.seek(85, stack);

        System.out.println(record.terminal.getKey());

        boolean didItWork = bst.insert(83);

        System.out.println(didItWork);

        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().left.getReference().getKey());
        System.out.println(bst.root.left.getReference().left.getReference().left.getReference().right.getReference().getKey());

        boolean didItDelete = bst.delete(83);

        if (bst.root.left.getReference().left.getReference().left.getReference().left.getReference() == null)
        {
            System.out.println("Its Null!");
        }

    }
}
