#include <iostream>
#include <atomic>
#include <vector>
#include <pthread.h>
#include <cstdint>

struct Node
{
  
    Node* left;
    Node* right;
    int key;
};

// This portion is from VasuAgrawal on Github to ensure last two bits can be used for flagging and tagging
// and address is maintained: pack in a single word.

#define PTR(x) (reinterpret_cast<Node*>(x))
#define LNG(x) (reinterpret_cast<unsigned long>(x))
#define BL(x)  (LNG(x) != 0)

#define GET_ADDR(Node) (PTR(LNG(Node) & (~(TAG_BIT | FLAG_BIT))))
#define GET_LEFT(Node) (GET_ADDR(Node)->left)
#define GET_RIGHT(Node) (GET_ADDR(Node)->right)

#define GET_TAG(Node) (BL(LNG(Node) & TAG_BIT))
#define GET_FLAG(Node) (BL(LNG(Node) & FLAG_BIT))
#define TAGGED(Node) (PTR(LNG(Node) | TAG_BIT))
#define FLAGGED(Node) (PTR(LNG(Node) | FLAG_BIT))
#define UNTAGGED(Node) (PTR(LNG(Node) & (~TAG_BIT)))
#define UNFLAGGED(Node) (PTR(LNG(Node) & (~FLAG_BIT)))

#define TAG(x) ((x) = TAGGED(x));
#define FLAG(x) ((x) = FLAGGED(x));

#define TAG_BIT 1
#define FLAG_BIT 2

// End of portion

struct SeekRecord
{
    Node* ancestor;
    Node* successor;
    Node* parent;
    Node* terminal; 
};

Node* getNextChildField(int key, Node* current)
{
    if (key < GET_ADDR(current)->key)
        return GET_LEFT(current);
    
    return GET_RIGHT(current);
}

SeekRecord* seek(Node* root, int key)
{
    Node* ancestor = root;
    Node* successor = GET_LEFT(root);
    Node* parent = GET_LEFT(root);
    Node* current = GET_ADDR(GET_LEFT(root));

    Node* childFieldAtParent = GET_LEFT(parent);
    Node* childFieldAtCurrent = GET_LEFT(current);

    Node* next = GET_ADDR(childFieldAtCurrent);
    
    while (next != nullptr)
    {
        if (!GET_TAG(childFieldAtParent))
        {
            ancestor = parent;
            successor = current;
        }

        parent = current;
        current = next;

        childFieldAtParent = childFieldAtCurrent;
        childFieldAtCurrent = getNextChildField(key, current);
        next = GET_ADDR(childFieldAtCurrent);
    }

    SeekRecord* seekRecord = new SeekRecord();
    seekRecord->ancestor = ancestor;
    seekRecord->successor = successor;
    seekRecord->parent = parent;
    seekRecord->terminal = current;

    return seekRecord;
}

bool search(Node* root, int key)
{
    SeekRecord* seekRecord = seek(root, key);

    if ((GET_ADDR(seekRecord->terminal)->key) == key)
        return true;
    
    return false;
}

bool insert(Node* root, int key)
{
    while (true)
    {
        SeekRecord* seekRecord = seek(root, key);
        if ((GET_ADDR(seekRecord->terminal)->key) != key)
        {
            Node* parent = GET_ADDR(seekRecord->parent);
            Node* terminal = GET_ADDR(seekRecord->terminal);
          
            Node** addressOfChildField = getNextChildField_ptr(key,parent);
            
            Node* newInternal = new Node(Math.max(terminal->key, key);
            Node* newLeaf = new Node(key);
            
                                         
        }
    }
}

int main()
{
    struct Node* node = (struct Node*) malloc(sizeof(struct Node));
    int x = 22;
    node->key = x;
    node->right = (struct Node*) malloc(sizeof(struct Node));
    int y = 18;
    node->right->key = y;
    Node* n = new Node();
    int z = 20;
    n->key = z;
    node->left = n;
   
    SeekRecord* seek1 = seek(node, 0);

    if (GET_TAG(seek1->successor))
        std::cout << GET_TAG(seek1->successor) << "tag"<< std::endl;
   
    std::cout << GET_FLAG(seek1->successor) << "flag"<< std::endl;
    std::cout << GET_ADDR(seek1->successor)->key << "val"<< std::endl;

    return 0;
}
