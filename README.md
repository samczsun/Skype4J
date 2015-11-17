# Skype4J

This is a Skype API for Java. It does not support P2P chats. You can tell whether you're in a P2P chat or not based on the output of the `/help` command. If it contains commands such as `/kickban`, you're in a P2P chat and should switch to cloud chats immediately (try using `/fork`)

Here is an example of this API in action

```java
Skype skype = new SkypeBuilder(username, password).withAllResources().build();
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

This project is licensed under the Apache 2.0 license

## Contributing

If you want to help out, thanks a lot! However, there are a few legalities to work out first.

Any contributions you wish to make must be accompanied by a Contributer License Agreement ("CLA").
This simply gives myself, or whoever is maintaining the project, the right to redistribute your contributions.

The CLA can be found in the root directory of the project, in the file called "CLA". Please read it carefully.

You only need to submit your CLA once, so if you've already signed a CLA there's no need to do it again.