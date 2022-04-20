import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicStampedReference;

public class TestAdd
{
    public static void main(String [] args)
    {
        ConcurrentBST bst = new ConcurrentBST();
        bst.root = new Node(100);
        Node temp1 = new Node(95);
        Node temp2 = new Node(100);
        Node temp3 = new Node(90);
        Node temp4 = new Node(95);
        Node temp5 = new Node(89);
        Node temp6 = new Node(94);
        Node temp7 = new Node(85);
        Node temp8 = new Node(89);
        Node temp9 = new Node(90);
        Node temp10 = new Node(94);

        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp3, 0);
        temp2.right = new AtomicStampedReference<>(temp4, 0);
        temp3.left = new AtomicStampedReference<>(temp5, 0);
        temp3.right = new AtomicStampedReference<>(temp6, 0);
        temp5.left = new AtomicStampedReference<>(temp7, 0);
        temp5.right = new AtomicStampedReference<>(temp8, 0);
        temp6.left = new AtomicStampedReference<>(temp9, 0);
        temp6.right = new AtomicStampedReference<>(temp10, 0);

        boolean work = bst.insert(88);

        if (work && bst.search(88))
        {
            System.out.println("True");
        }

        work = bst.insert(84);

        if (work && bst.search(84))
        {
            System.out.println("True");
        }

        work = bst.insert(93);

        if (work && bst.search(93))
        {
            System.out.println("True");
        }
    }
}
