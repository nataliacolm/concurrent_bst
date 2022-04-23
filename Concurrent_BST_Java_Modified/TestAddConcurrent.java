import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicStampedReference;


class AddThreads implements Runnable
{
    private ConcurrentBSTModify bst;
    private Random rand;
    private boolean canRun;
    private int id;
    private int interation;
    private int maxInteration = 1000;
    //private int maxInteration = 10000;
    //private int maxInteration = 100000;

    AddThreads(ConcurrentBSTModify bst, int id)
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
            //System.out.println(interation + " " + id);
            boolean finished = bst.insert(value);

            if (bst.search(value))
            {
                interation++;
            }

            if (interation == maxInteration)
            {
                canRun = false;
            }
        }
    }
}

public class TestAddConcurrent
{
    public static void main(String agrs [])
    {
        ConcurrentBSTModify bst = new ConcurrentBSTModify();
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

        AddThreads test1 = new AddThreads(bst, 1);
        AddThreads test2 = new AddThreads(bst, 2);
        AddThreads test3 = new AddThreads(bst, 3);
        AddThreads test4 = new AddThreads(bst, 4);

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
