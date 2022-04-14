#include <iostream>
#include <atomic>
#include <vector>
#include <pthread.h>

struct Node
{
    int key;
    Node* left;
    Node* right;

    Node(int key) : key(key), left(nullptr), right(nullptr) {}
};

/*

    Bug: a call to TAG(node), FLAG(node), UNTAGGED(node), UNFLAGGED(node) will cause
    the key value to be lost. Therefore, in order to remedy this, we can reassign the key 
    value after making the calls mentioned above
*/

// This portion is from VasuAgrawal on Github to ensure last two bits can be used for flagging and tagging
// and address is maintained: pack in a single word.


#define PTR(x) (reinterpret_cast<Node*>(x))
#define LNG(x) (reinterpret_cast<unsigned long>(x))
#define BL(x)  (LNG(x) != 0)

#define PACK_CHILD(p,c,d) ((p)->d = PTR(LNG(c) | (LNG((p)->d) & 3)))
#define PACK_LEFT(p,c) PACK_CHILD(p,c,left)
#define PACK_RIGHT(p,c) PACK_CHILD(p,c,right)

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
#define UNFLAG(x) ((x) = UNFLAGGED(x));

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
    if (key < current->key)
        return GET_LEFT(current);
    
    return GET_RIGHT(current);
}

bool get_flag(uintptr_t p)
{
    return p & 1;
}

void *get_pointer(uintptr_t p)
{
    //uintptr_t t = reinterpret_cast<Node*>(p);

    return (void *)(p & (UINTPTR_MAX ^ 1));
}

Node* set_flag(Node* p, bool value)
{
    uintptr_t t = reinterpret_cast<uintptr_t>(p);
    t = (t & (~2)) | value;

    return reinterpret_cast<Node*>(t);
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
    std::cout << GET_ADDR(successor) << "succ"<< std::endl;

    std::cout << successor->key << "first"<< std::endl;
    //FLAGGED(successor);
    //TAGGED(successor);
    std::cout << successor->key << "second"<< std::endl;
    //successor->key = 20;
    //UNFLAG(successor);
    std::cout << successor->key << "third"<< std::endl;
    //successor->key.value = 20;
    successor = set_flag(successor, true);
    std::cout << successor->key << "fourth"<< std::endl;
    UNFLAG(successor);

    /*

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
    */

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

    if (seekRecord->terminal->key == key)
        return true;
    
    return false;
}


int main()
{
    Node* node = new Node(20);
    //node->key = new Key(20);
    node->right = new Node(18);
    //node->right->key = 18;
    Node* n = new Node(20);
    //n->key = new Key(20);
    node->left = n;
    //PACK_LEFT(node, n);


    
    
    
   
    SeekRecord* seek1 = seek(node, 0);

    if (GET_TAG(seek1->successor))
        std::cout << GET_TAG(seek1->successor) << "tag"<< std::endl;
    //std::cout << GET_TAG(seek1->successor) << "tag"<< std::endl;
    std::cout << GET_FLAG(seek1->successor) << "flag"<< std::endl;
    std::cout << seek1->successor->key << "val"<< std::endl;
    std::cout << GET_ADDR(seek1->successor) << "succ"<< std::endl;

    //std :: cout << alignof(node) << "\n";

    return 0;
}