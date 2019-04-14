# SDIS-P1


## From the src/ folder:

### Compile the Peer:
javac main/java/peer/Peer.java


### Compile the TestApp:
javac main/java/testapp/TestApp.java

### Run RMI:
rmiregistry &


### Run Peer that launches RMI:
java main/java/peer/Peer <protocol_version> <peer_id> <RMI_Access_Point> <MC_IP>:<MP_PORT> <MDB_IP>:<MDP_PORT> <MDR_IP>:<MDR_PORT>
ex:
java main/java/peer/Peer 1.0 0 192.168.0.1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002

### Run normal peers:
java main/java/peer/Peer <protocol_version> <peer_id> <MC_IP>:<MP_PORT> <MDB_IP>:<MDP_PORT> <MDR_IP>:<MDR_PORT>
ex:
java main/java/peer/Peer 1.0 1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002



### Run a TestApp:

## Backup:
java main/java/testapp/TestApp <rmi_peer_ip>/obj BACKUP <file_path> <replication_degree>
ex:
java main/java/testapp/TestApp 127.0.0.1/obj BACKUP /Users/zemiguel/IdeaProjects/SDIS-P1/src/files/test1.pdf 1

## Restore:
java main/java/testapp/TestApp <rmi_peer_ip>/obj RESTORE <file_name> <replication_degree>
ex:
java main/java/testapp/TestApp 127.0.0.1/obj RESTORE test1.pdf

## Delete:
java main/java/testapp/TestApp <rmi_peer_ip>/obj DELETE <file_name>
ex:
java main/java/testapp/TestApp 127.0.0.1/obj DELETE test1.pdf

## Reclaim:
java main/java/testapp/TestApp <rmi_peer_ip>/obj RECLAIM <amount>
ex:
java main/java/testapp/TestApp 127.0.0.1/obj RECLAIM 0

## State:
java main/java/testapp/TestApp <rmi_peer_ip>/obj STATE
ex:
java main/java/testapp/TestApp 127.0.0.1/obj STATE
