import java.util.*;
import java.io.*;

class Leaf
{
    private boolean flag;
    private boolean tag;
    public Node node;

    Leaf (Node node)
    {
        this.node = node;
        this.flag = false;
        this.tag = false;
    }

    public boolean getFlag()
    {
        return this.flag;
    }

    public boolean getTag()
    {
        return this.tag;
    }

    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    public void setTag(boolean tag)
    {
        this.tag = tag;
    }
}

class Node
{
    private int key;
    public Leaf left;
    public Leaf right;

    Node (int key)
    {
        this.key = key;
        this.left = null;
        this.right = null;
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
    public Node bst;
    public SeekRecord seekRecord;

    public Leaf getNextChildNode(int key, Node current)
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
        Node ancestor = bst;
        Node succesor = bst.left.node;
        Node parent = bst.left.node;
        Node current = parent.left.node;

        Leaf childFieldAtParent = parent.left;
        Leaf childFieldAtCurrent = current.left;
        Node next = childFieldAtParent.node;

        while (next != null)
        {
            if (childFieldAtParent.getTag() == false)
            {
                ancestor = parent;
                succesor = current;
            }

            parent = current;
            current = next;

            childFieldAtParent = childFieldAtCurrent;
            childFieldAtCurrent = getNextChildNode(key, current);

            if (childFieldAtCurrent == null)
            {
                next = null;
            }
            else
            {
                next = childFieldAtCurrent.node;
            }
        }

        seekRecord.ancestor = ancestor;
        seekRecord.succesor = succesor;
        seekRecord.parent = parent;
        seekRecord.terminal = current;
        return;
    }

    public static void main (String [] args)
    {
        // Set initialize the Seek Record
    }
}
