#include "ConcurrentBST.h"

// Reference: https://stackoverflow.com/questions/52273110/how-do-i-write-a-unit-test-in-c
#define IS_TRUE(x) { if (!(x)) std::cout << __FUNCTION__ << " failed on line " << __LINE__ << std::endl; }

void test_insert(Node* root)
{
    IS_TRUE(insert(root, 25));
    IS_TRUE(insert(root, 26));
    IS_TRUE(insert(root, 27));

    IS_TRUE(search(root, 25));
    IS_TRUE(search(root, 26));
    IS_TRUE(search(root, 27));

    IS_TRUE(!insert(root, 25));
    IS_TRUE(!insert(root, 26));
    IS_TRUE(!insert(root, 27));
}

void test_remove(Node* root)
{
    IS_TRUE(remove(root, 25));
    IS_TRUE(remove(root, 26));
    IS_TRUE(remove(root, 27));

    IS_TRUE(!search(root, 25));
    IS_TRUE(!search(root, 26));
    IS_TRUE(!search(root, 27));

    IS_TRUE(!remove(root, 25));
    IS_TRUE(!remove(root, 25));
    IS_TRUE(!remove(root, 27));
}

void test_insert_and_remove(Node* root)
{
    IS_TRUE(insert(root, 30));
    IS_TRUE(remove(root, 30));
    IS_TRUE(!remove(root, 30));

    // Test that subtree still exists even when parent is deleted.
    IS_TRUE(insert(root, 1000));
    IS_TRUE(insert(root, 1001));
    IS_TRUE(insert(root, 1003));
    IS_TRUE(remove(root, 1000));
    IS_TRUE(search(root, 1001));
    IS_TRUE(search(root, 1003));
    IS_TRUE(!search(root, 1000));
    IS_TRUE(insert(root, 1000));
    IS_TRUE(search(root, 1000));
}

int main()
{
    
    // sentinel keys are initialized here. They are never to be removed.
    struct Node* node = new Node();
    node->key = 600000;
    node->left = new Node();
    node->left->key = 599999;
    Node* n = new Node();
    n->key = 600000;
    node->right = n;
    node->left->right = new Node();
    node->left->left = new Node();
    node->left->right->key = 599999;
    node->left->left->key = 599998;

    // When testing, showing no output means all test cases passed. Else, 
    // the line the test case failed will appear on the console.

    // Testing sequential implementation

    test_insert(node);
    test_remove(node);
    test_insert_and_remove(node);

    return 0;
}
