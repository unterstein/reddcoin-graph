# Whats this?
The crypto currency [Reddcoin](https://www.reddcoin.com/) is a social currency
that makes digital currency easy for the general public. Reddcoin achieves this by integrating a digital
currency platform seamlessly with all major social networks to make the process of sending and receiving money
fun and rewarding for everyone.

I am really enjoying the Reddcoin [Twitter Bot](https://twitter.com/tipreddcoin)! You can easily tweet something like
`@tipreddcoin tip @WhateverUser 50 RDD` to transfer 50 Reddcoin from your personal TipBot wallet to the receiver`s TipBot wallet.
It makes it incredibly easy to appreciate other and g


This is a highly connected and social activity and you can extract this activity to a graph. I am also really enjoying graph databases, like
[Neo4j](https://neo4j.com/). With this technology it is possible to model your data as graph, which fits this tipping behavior very well.

# The graph
If you follow the path of code in this repository, you will see that this project basically contains two elements.

1. A program to get all tweets by the Redcoin Twitter bot and stores them locally
2. A program that takes this data and transforms it into a connected graph, where users are nodes and tips are edges.

You can see an example here:

![Graph](./img/connections.png)

With the Neo4j UI you can easily browse the connections between users, tips and others users. You can pick a starting node
and navigate further from this starting point.

# Example cyphers
MATCH (sender: User)-[r:TIPS]->() return sender.name, count(r) as c order by c desc;

MATCH ()-[r:TIPS]->(receiver: User) return receiver.name, count(r) as c order by c desc;

MATCH ()-[r:TIPS]->() return count(r)

MATCH ()-[r:TIPS]->() return sum(r.amount)
