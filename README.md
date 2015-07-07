# Skype4J

This is a Skype API for Java. It does not support P2P chats. You can tell whether you're in a P2P chat or not based on the output of the `/help` command. If it contains commands such as `/kickban`, you're in a P2P chat and should switch to cloud chats immediately (try using `/fork`)

This is also very much a work in progress. As such, API methods are not guarenteed to remain the same. However, if you stay out of the `com.samczsun.skype4j.internal` package you should be good

Here is an example of this API in action

```java
Skype skype = Skype.login(username, password);
skype.getEventDispatcher().registerListener(new Listener() {
  @EventHandler
  public void onMessage(MessageReceivedEvent e) {
    System.out.println("Got message: " + e.getMessage().getText());
  }
});
skype.subscribe();
// Do stuff
skype.logout();
```
## Maven

Maven is used for dependency management and deployment. To build with this project as a dependency, you must add the following repository to your POM

```
<repository>
  <id>samczsun-repo</id>
  <url>http://r.zk.ms/content/repositories/snapshots</url>
</repository>
```

Then add the following dependency

```
<dependency>
  <groupId>com.samczsun</groupId>
  <artifactId>skype4j</artifactId>
  <version>0.0.3-SNAPSHOT</version>
</dependency>
```

## Licensing

This project is licensed under the GPLv3 licence
