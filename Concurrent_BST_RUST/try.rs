//use std::thread;
//use std::time::Duration;
use std::sync::atomic::AtomicI32;

//#[derive(Copy, Clone)]
struct Node
{
    key: i32,
    left: Option<Box<Node>>,
    right: Option<Box<Node>>
}

impl Node
{
    fn get_key(&mut self) -> i32
    {
        return self.key;
    }

    fn get_left(&mut self) -> Box<Node>
    {
        match self.left
        {
            Some(ref node) => return *node
        }
    }

    fn get_right(&mut self) -> Box<Node>
    {
        match self.right
        {
            Some(ref node) => return *node
        }
    }
}

struct SeekRecord<'a>
{
    parent: &'a Node,
    current: &'a Node
}

fn seek(node: &Node) -> SeekRecord
{
    //let temp1 = Node {key: 10, left: Some(Box::new(Node {key: 5, left: None, right: None})), right: None};

    return SeekRecord {parent: node, current: node};
}

fn main()
{
    let node = Node {key: 100,
        left: Some(Box::new(Node {key: 95,
            left: Some(Box::new(Node {key: 90, left: None, right: None})),
            right: Some(Box::new(Node {key: 95, left: None, right: None}))})),
        right: Some(Box::new(Node {key: 100, left: None, right: None}))};

    let record: SeekRecord = seek(&node);
}
