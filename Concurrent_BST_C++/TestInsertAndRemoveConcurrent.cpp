#include "ConcurrentBST.h"

#include <thread>
#include <vector>
#include <cstdlib>
#include <atomic>
#include <chrono>

using namespace std;
using namespace std::chrono;

#define MAX_ITERATION1 1000
#define MAX_ITERATION2 10000
#define MAX_ITERATION3 100000

atomic_int num_iterations = 0;
atomic_int build_tree_iterations = 0;

void run(Node* root)
{
    // Edit MAX_ITERATION here.
    for (int i = num_iterations; num_iterations < MAX_ITERATION1; num_iterations++)
    {
        int rand_var = (rand() % 90000);

        bool insert_node = true;

        if ((rand() % 2) == 1)
            insert_node = false;

        if (insert_node)
        {
            insert(root, rand_var);
            //cout << "add" << endl;
        }

        else
        {
            remove(root, rand_var);
            //cout << "delete" << endl;

        }
    }

    cout << "done! ";
}

int main()
{
    srand(time(0));
    
    // sentinel keys are initialized here. They are never to be removed.
    struct Node* node = new Node();
    node->key = 100000;
    node->left = new Node();
    node->left->key = 95000;
    Node* n = new Node();
    n->key = 100000;
    node->right = n;
    node->left->right = new Node();
    node->left->left = new Node();
    node->left->right->key = 95000;
    node->left->left->key = 90000;
    
    // Edit number of threads here.
    int num_threads = 4;

    std::vector<std::thread> threads;

    auto start = high_resolution_clock::now();

    for (int i = 0; i < num_threads; i++)
    {
        std::thread t (run, node);
        threads.push_back(std::move(t));
    }

    for (auto& t : threads)
    {
        t.join();
    }

    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<microseconds>(stop - start);
    double res = duration.count() * .001;
    cout << endl;
    cout << res << endl;

    return 0;
}