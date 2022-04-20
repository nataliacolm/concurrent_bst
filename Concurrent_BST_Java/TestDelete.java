import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicStampedReference;

public class TestDelete
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
        Node temp6 = new Node(90);
        Node temp7 = new Node(85);
        Node temp8 = new Node(89);
        Node temp9 = new Node(70);
        Node temp10 = new Node(85);
        Node temp11 = new Node(68);
        Node temp12 = new Node(79);
        Node temp13 = new Node(65);
        Node temp14 = new Node(68);
        Node temp15 = new Node(70);
        Node temp16 = new Node(79);

        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp3, 0);
        temp1.right = new AtomicStampedReference<>(temp4, 0);
        temp3.left = new AtomicStampedReference<>(temp5, 0);
        temp3.right = new AtomicStampedReference<>(temp6, 0);
        temp5.left = new AtomicStampedReference<>(temp7, 0);
        temp5.right = new AtomicStampedReference<>(temp8, 0);
        temp7.left = new AtomicStampedReference<>(temp9, 0);
        temp7.right = new AtomicStampedReference<>(temp10, 0);
        temp9.left = new AtomicStampedReference<>(temp11, 0);
        temp9.right = new AtomicStampedReference<>(temp12, 0);
        temp11.left = new AtomicStampedReference<>(temp13, 0);
        temp11.right = new AtomicStampedReference<>(temp14, 0);
        temp12.left = new AtomicStampedReference<>(temp15, 0);
        temp12.right = new AtomicStampedReference<>(temp16, 0);

        //int x = bst.root.left.getReference().left.getReference().right.getReference().left.getReference().right.getReference().getKey();

        //SeekRecord record = bst.seek(93);

        //System.out.println(record.terminal.getKey());

        boolean work = bst.delete(65);

        if (!bst.search(65))
        {
            System.out.println("True for 65");
        }

        System.out.println("=====================================");

        work = bst.delete(79);

        if (!bst.search(79))
        {
            System.out.println("True for 79");
        }

        System.out.println("=====================================");

        work = bst.delete(68);

        if (!bst.search(68))
        {
            System.out.println("True for 68");
        }

        System.out.println("=====================================");

        work = bst.delete(70);

        if (!bst.search(70))
        {
            System.out.println("True for 70");
        }

        System.out.println("=====================================");

        work = bst.delete(85);

        if (!bst.search(85))
        {
            System.out.println("True for 85");
        }

        System.out.println("=====================================");

        work = bst.delete(89);

        if (!bst.search(89))
        {
            System.out.println("True for 89");
        }
    }
}
