find . -type f -name '*.class' -delete

javac -cp /Users/zemiguel/IdeaProjects/SDIS-P1/src/ main/java/peer/Peer.java

java -Djava.net.preferIPv4Stack=true -Djava.rmi.server.codebase=file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/main/java/service/ main/java/peer/Peer 1.0 0 192.168.0.1 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002