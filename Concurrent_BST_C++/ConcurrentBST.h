#define CONCURRENTBST_H

#include <iostream>
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

#define INJECTION 1
#define CLEANUP 0

// End of portion

/*

    // ~~~~~~~~~~~NOTES to help understand bit manipulation in program: ~~~~~~~~~~~~~


    // node -> word
    // GET_ADDR(node) -> address as we know it not packed into a word.

    // If we only Node* test = GET_ADDR(node) then we cannot see node's flag or tag state through test.
    // If we do Node* test = node, then we can see the node's flag or tag state through test.

    // Note the differences in call between these portions (a, b):

    // A. Both return the same result!
    std::cout << GET_ADDR(node->left) << "node_addr->left"<< std::endl;
    std::cout << GET_ADDR(GET_LEFT(node)) << "node->left"<< std::endl;

    // B. Both return the same result!
    std::cout << GET_ADDR(node)->left << "(node)addr->left"<< std::endl; // Differs from the above
    std::cout << node->left << "(node)->left"<< std::endl; // Differs from the above

    // Returns the same result:
    std::cout << GET_ADDR(node) << "node_addr1"<< std::endl;
    std::cout << GET_ADDR(GET_ADDR(node)) << "node in node!"<< std::endl;

    // Still must do GET_ADDR(node)->key to view key contents if we are not given an address.

*/

struct SeekRecord
{
    Node* ancestor;
    Node* successor;
    Node* parent;
    Node* terminal; 
};

// In this case current holds an address and not the word! Extract key as normal.
Node* getNextChildField(int key, Node* current)
{
    if (key < GET_ADDR(current)->key)
        return current->left;
    
    return current->right;
}

Node** getNextChildField_ptr(int key, Node* current)
{
    // to greater than
    if (key < GET_ADDR(current)->key)
    {
        return (&(current->left));
    }

    return (&(current->right));
}

Node** getAddressOfNextChildField(Node* node, Node* child)
{
    if (GET_ADDR(child)->key < GET_ADDR(node)->key)
        return &(node->left);
    
    return &(node->right);
}

// Notice the difference from getAddressOfNextChildField
Node** getAddressofSiblingChildField(Node* node, Node* child)
{
    if (GET_ADDR(child)->key < GET_ADDR(node)->key)
        return &(node->right);
    
    return &(node->left);
}

SeekRecord seek(Node* root, int key)
{
    Node* ancestor = root;
    Node* successor = root->left;
    Node* parent = root->left;
    Node* current = GET_ADDR(root->left);

    Node* childFieldAtParent = parent->left;
    Node* childFieldAtCurrent = current;
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

    SeekRecord seekRecord;
    seekRecord.ancestor = ancestor;
    seekRecord.successor = successor;
    seekRecord.parent = parent;
    seekRecord.terminal = current;

    return seekRecord;
}

bool cleanup(Node* root, SeekRecord seekRecord)
{
    Node* ancestor = seekRecord.ancestor;
    Node* successor = seekRecord.successor;
    Node* parent = seekRecord.parent;
    Node* terminal = seekRecord.terminal;

    Node** addressOfSuccessorField = getAddressOfNextChildField(ancestor, successor);
    Node** addressOfChildField = getAddressOfNextChildField(parent, terminal);
    Node** addressOfSiblingField = getAddressofSiblingChildField(parent, terminal); //

    if (!GET_FLAG(*addressOfChildField))
    {
        addressOfSiblingField = addressOfChildField;
    }

    if (!GET_TAG(*addressOfChildField))
    {
        // reference: https://stackoverflow.com/questions/22974382/assembly-intrinsic-for-atomic-bit-test-and-set-bts
        __sync_fetch_and_or(addressOfSiblingField, TAGGED(*addressOfSiblingField));
    }

    bool result = __sync_bool_compare_and_swap(addressOfSuccessorField, GET_ADDR(successor), UNTAGGED(*addressOfSiblingField));
    return result;
}

bool search(Node* root, int key)
{
    SeekRecord seekRecord = seek(root, key);

    if ((GET_ADDR(seekRecord.terminal)->key) == key)
        return true;
    
    return false;
}


bool remove(Node* root, int key)
{
    int mode = INJECTION;
    while (true)
    {
        SeekRecord seekRecord = seek(root, key);
        Node* parent = seekRecord.parent;
        Node** addressOfChildField = (getNextChildField_ptr(key, parent));
        Node* terminal = nullptr;

        if (mode == INJECTION)
        {
            terminal = seekRecord.terminal;

            if (GET_ADDR(terminal)->key != key)
            {
                return false;
            }
                
            bool result = __sync_bool_compare_and_swap(addressOfChildField, GET_ADDR(terminal), FLAGGED(UNTAGGED(terminal)));

            if (result)
            {
                mode = CLEANUP;
                bool done = cleanup(root, seekRecord);

                if (done)
                    return true;
            }

            else
            {
                if (GET_ADDR(*addressOfChildField) == GET_ADDR(terminal) && (GET_FLAG(*addressOfChildField) || GET_TAG(*addressOfChildField)))
                    cleanup(root, seekRecord);
            }
        }

        else
        {
            if (seekRecord.terminal != terminal)
                return true;
            else
            {
                bool done = cleanup(root, seekRecord); 
                if (done)
                    return true;
            }
        }
    }
}


bool insert(Node* root, int key)
{
    while (true)
    {
        SeekRecord seekRecord = seek(root, key);
        Node* parent = seekRecord.parent;
        Node* terminal = seekRecord.terminal;

        if ((GET_ADDR(seekRecord.terminal)->key) != key)
        {
            Node* parent = seekRecord.parent;
            Node* terminal = seekRecord.terminal;
          
            Node** addressOfChildField = getNextChildField_ptr(key, parent);
            
            Node* newInternal = new Node();

            if (GET_ADDR(terminal)->key > key)
                newInternal->key = GET_ADDR(terminal)->key;
            else
                newInternal->key = key;

            Node* newLeaf = new Node();
            newLeaf->key = key;

            if (key > GET_ADDR(terminal)->key)
            {
                newInternal->left = terminal;
                newInternal->right = newLeaf;
            }

            else
            {
                newInternal->left = newLeaf;
                newInternal->right = terminal;
            }
        
            bool result = __sync_bool_compare_and_swap(addressOfChildField, terminal, newInternal);

            if (result)
                return true;
            else
            {
                if (GET_ADDR(addressOfChildField) == GET_ADDR(terminal) && (GET_FLAG(addressOfChildField) || GET_TAG(addressOfChildField)))
                {
                    cleanup(root, seekRecord);
                }
            }
        }

        // Already exists in the tree
        else
            return false;
    }
}
