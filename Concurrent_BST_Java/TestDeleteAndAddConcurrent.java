import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicStampedReference;


class DeleteAndAddThreads implements Runnable
{
    private ConcurrentBST bst;
    private Random rand;
    private boolean canRun;
    private int interation;
    private int id;
    //private int maxInteration = 1000;
    //private int maxInteration = 10000;
    private int maxInteration = 100000;

    DeleteAndAddThreads(ConcurrentBST bst, int id)
    {
        this.bst = bst;
        this.id = id;
        rand = new Random();
        canRun = true;
        interation = 0;
    }

    @Override
    public void run()
    {
        while (canRun)
        {
            int value = rand.nextInt(90000);
            int job = rand.nextInt(2);

            if (job == 0)
            {
               boolean finished = bst.delete(value);
               interation++;
            }
            else
            {
                boolean finished = bst.insert(value);
                if (bst.search(value))
                {
                    interation++;
                }
            }

            if (interation == maxInteration)
            {
                canRun = false;
            }
        }
    }
}

public class TestDeleteAndAddConcurrent
{
    public static void main(String agrs [])
    {
        ConcurrentBST bst = new ConcurrentBST();
        long start = System.nanoTime();

        // Create the Initial Tree
        bst.root = new Node(100000);
        Node temp1 = new Node(95000);
        Node temp2 = new Node(100000);
        Node temp3 = new Node(90000);
        Node temp4 = new Node(95000);

        bst.root.left = new AtomicStampedReference<>(temp1, 0);
        bst.root.right = new AtomicStampedReference<>(temp2, 0);
        temp1.left = new AtomicStampedReference<>(temp3, 0);
        temp1.right = new AtomicStampedReference<>(temp4, 0);

        DeleteAndAddThreads test1 = new DeleteAndAddThreads(bst, 1);
        DeleteAndAddThreads test2 = new DeleteAndAddThreads(bst, 2);
        DeleteAndAddThreads test3 = new DeleteAndAddThreads(bst, 3);
        DeleteAndAddThreads test4 = new DeleteAndAddThreads(bst, 4);

        Thread thread1 = new Thread(test1);
        Thread thread2 = new Thread(test2);
        Thread thread3 = new Thread(test3);
        Thread thread4 = new Thread(test4);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        try
        {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        }
        catch (InterruptedException error)
        {
            error.printStackTrace();
        }

        long end = System.nanoTime();
        long exectution = end - start;
        double convert = exectution / 1000000;
        System.out.println("Execution Time: " + Double.toString(convert) + " Miliseconds");
    }
}
