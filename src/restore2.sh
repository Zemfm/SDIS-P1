javac -cp /Users/zemiguel/IdeaProjects/SDIS-P1/src/ main/java/testapp/TestApp.java

java -Djava.net.preferIPv4Stack=true -Djava.rmi.serv.codebase=file:/Users/zemiguel/IdeaProjects/SDIS-P1/src/main/java/service/ main/java/testapp/TestApp 127.0.0.1/obj RESTORE test2.txt