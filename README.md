# Skype4J

This is a Skype API for Java. It does not support P2P chats. You can tell whether you're in a P2P chat or not based on the output of the `/help` command. If it contains commands such as `/kickban`, you're in a P2P chat and should switch to cloud chats immediately (try using `/fork`)

Here is an example of this API in action

```java
Skype skype = Skype.login(username, password);
skype.getEventDispatcher().registerListener(new Listener() {
  @EventHandler
  public void onMessage(MessageReceivedEvent e) {
    System.out.println("Got message: " + e.getMessage().getMessage());
  }
});
skype.subscribe();
// Do stuff
skype.logout();
```
## Maven

Maven is used for dependency management and deployment. In the near future this project will be added into the central repository.

In the meantime, please clone this project and install it locally.

Then add the following dependency

```
<dependency>
  <groupId>com.samczsun</groupId>
  <artifactId>skype4j</artifactId>
  <version>0.0.9-SNAPSHOT</version>
</dependency>
```

## Licensing

This project is licensed under the GPLv3 licence

## Contributing

If you want to help out, thanks a lot! Please make sure you license your contributions under the GPLv3 license though.
