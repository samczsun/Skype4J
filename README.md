# Skype4J

This is a Skype API for Java. It currently does not support P2P chats, but plans to do so in the future (with native dependencies)

This is also very much a work in progress. As such, API methods are not guarenteed to remain the same. However, if you stay out of the `com.samczsun.skype4j.internal` package you should be good

Here is an example of this API in action

```java
Skype skype = SkypeClient.create(username, password).client(Client.WEB).login();
skype.getEventDispatcher().registerListener(new Listener() {
  @EventHandler
  public void onMessage(MessageReceivedEvent e) {
    System.out.println("Got message: " + e.getMessage().getMessage());
  }
});
skype.subscribe();
```
## Licensing

This project is licensed under the GPLv3 licence
