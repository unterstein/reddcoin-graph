

# Example cyphers
MATCH (sender: User)-[r:TIPS]->() return sender.name, count(r) as c order by c desc;

MATCH ()-[r:TIPS]->(receiver: User) return receiver.name, count(r) as c order by c desc;

MATCH ()-[r:TIPS]->() return count(r.amount)