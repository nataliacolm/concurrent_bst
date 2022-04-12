#include <iostream>

struct Node
{
    int key;
    Node* left;
    Node* right;
};

struct SeekRecord
{
    Node* ancestor;
    Node* successor;
    Node* parent;
    Node* terminal; 
};

int main()
{
    Node *node;
    node->key = 4;
    std::cout << sizeof(*node);
    return 0;
}