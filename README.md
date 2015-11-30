# Skype4J

[![Build Status](http://ci.samczsun.com/buildStatus/icon?job=Skype4J)](http://ci.samczsun.com/job/Skype4J/)

This is a Skype API for Java. It does not support P2P chats. You can tell whether you're in a P2P chat or not based on the output of the `/help` command. If it contains commands such as `/kickban`, you're in a P2P chat and should switch to cloud chats immediately (try using `/fork`)

Here is an example of using this API to log into your Skype account.

```java
Skype skype = new SkypeBuilder(username, password).withAllResources().build();
skype.login();
skype.getEventDispatcher().registerListener(new Listener() {
  @EventHandler
  public void onMessage(MessageReceivedEvent e) {
    System.out.println("Got message: " + e.getMessage().getContent());
  }
});
skype.subscribe();
// Do stuff
skype.logout();
```

If you want to use a guest account, you can do that too

```java
Skype skype = new SkypeBuilder("Skype4JGuest").withChat("19:42abed183a95456ea1de9e2f7356163c@thread.skype").withAllResources().build();
skype.login();
skype.getEventDispatcher().registerListener(new Listener() {
  @EventHandler
  public void onMessage(MessageReceivedEvent e) {
    System.out.println("Got message: " + e.getMessage().getContent());
  }
});
skype.subscribe();
// Do stuff
skype.logout();
```

Notice how both examples are extremely similar. You can switch between guest accounts and regular accounts with ease thanks to abstractation.
## Maven

Maven is used for dependency management and deployment. To use this API, simply add the following to your pom.xml

```
<dependency>
  <groupId>com.samczsun</groupId>
  <artifactId>skype4j</artifactId>
  <version>0.1.4</version>
</dependency>
```

You can also download the latest build of this project from [Jenkins](http://ci.samczsun.com/job/Skype4J/) 
## Licensing

This project is licensed under the Apache 2.0 license

## Contributing

If you want to help out, thanks a lot! However, there are a few legalities to work out first.

Any contributions you wish to make must be accompanied by a Contributer License Agreement ("CLA").
This simply gives myself, or whoever is maintaining the project, the right to redistribute your contributions.

The CLA can be found in the root directory of the project, in the file called "CLA". Please read it carefully.

You only need to submit your CLA once, so if you've already signed a CLA there's no need to do it again.