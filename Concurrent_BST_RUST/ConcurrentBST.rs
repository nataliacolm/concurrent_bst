//use std::thread;
//use std::time::Duration;
use std::sync::atomic::AtomicI32;

#[derive(Copy, Clone)]
struct Node
{
    key: i32,
    mark: i32,
    left: Option<Box<Node>>,
    right: Option<Box<Node>>
}

struct SeekRecord
{
    ancestor: Box<Node>,
    sucessor: Box<Node>,
    parent: Box<Node>,
    current: Box<Node>
}

impl Node
{
    fn insert (&mut self, key:i32)
    {
        if key >= self.key
        {
            match self.left
            {
                None => self.left = Some(Box::new(Node {key: key, mark: 0, left: None, right: None})),
                Some(ref mut node) => node.insert(key)
            }
        }
        else
        {
            match self.right
            {
                None => self.right = Some(Box::new(Node {key: key, mark: 0, left: None, right: None})),
                Some(ref mut node) => node.insert(key)
            }
        }
    }

    fn traverse(&mut self)
    {
        println!("{}", self.key);

        //let mut node : &mut Node = self;

        loop
        {
            match self.left
            {
                None => break,
                Some(ref mut node) => {
                    node.traverse();
                    break
                }
            }
        } // End of Loop

        loop
        {
            match self.right
            {
                None => break,
                Some(ref mut node) => {
                    node.traverse();
                    break
                }
            }
        } // End of Loop

        println!("Done!");
    }

    fn get_left_child(&mut self) -> Box<Node>
    {
        match self.left
        {
            Some(node) => return node
        }
    }
}

fn unbox<T>(value: Box<T>) -> T {
    *value
}

fn seek(node: &Node) -> SeekRecord
{
    let mut ancestor = node;
    let mut sucessor = unbox(node.get_left_child());
    let mut parent = unbox(node.get_left_child());
    let mut current = unbox(parent.get_left_child());
    let mut size = 0;

    loop
    {
        break;
    }

    if size == 0
    {
        return SeekRecord {ancestor: Box::new(*ancestor), sucessor: Box::new(sucessor), parent: Box::new(parent), current: Box::new(current)};
    }

    else
    {
        return SeekRecord {ancestor: Box::new(*ancestor), sucessor: Box::new(sucessor), parent: Box::new(parent), current: Box::new(current)};
    }
}

fn main()
{
    let mut root = Node {key: 100, mark: 0, left: None, right: None};
    let mut array : [i32; 4] = [100, 95, 95, 90];
    let mut size = 0;

    for x in array
    {
        root.insert(x);
        size = size + 1;
    }

    println!("{}", size);
    root.traverse();

    let seek_record = seek(&root);
}
