javac -cp /Users/zemiguel/IdeaProjects/SDIS-P1/src/ main/java/client/Client.java

java -Djava.net.preferIPv4Stack=true -Djava.rmi.serv.codebase=file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/main/java/service/ main/java/client/Client 127.0.0.1/obj BACKUP /Users/zemiguel/IdeaProjects/SDIS-P1/src/files/test1.pdf 1