# Skype4J

[![Build Status](https://ci.samczsun.com/buildStatus/icon?job=Skype4J)](https://ci.samczsun.com/job/Skype4J/)

This is a Skype API for Java. It does not support P2P chats. To find out what type of chat you're in, simply type `/get name` into the chat window and press Enter. If the group name response starts with "19:...", you're in a cloud-based chat; if the group name response starts with "#skypename...", you're in a P2P-based chat. To switch to cloud chat you can use `/fork` or re-create chat in web.skype.com and re-invite members.


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

For more examples, please consult the [wiki](https://github.com/samczsun/Skype4J/wiki)

## Maven

Maven is used for dependency management and deployment. To use this API, simply add the following to your pom.xml

```
<dependency>
  <groupId>com.samczsun</groupId>
  <artifactId>skype4j</artifactId>
  <version>0.1.5</version>
</dependency>
```

You can also download the latest build of this project from [Jenkins](https://ci.samczsun.com/job/Skype4J/)  
If you want to use Maven with the latest build and don't want to manually install, [JitPack](https://jitpack.io/#samczsun/skype4j) may do the trick.

## JavaDocs

JavaDocs can be found [here](https://samczsun.github.io/Skype4J/)

## Licensing

This project is licensed under the Apache 2.0 license

## Contributing

If you want to help out, thanks a lot! However, there are a few legalities to work out first.

Any contributions you wish to make must be accompanied by a Contributer License Agreement ("CLA").
This simply gives myself, or whoever is maintaining the project, the right to redistribute your contributions.

The CLA can be found in the root directory of the project, in the file called "CLA". Please read it carefully.

You only need to submit your CLA once, so if you've already signed a CLA there's no need to do it again.

## Acknowledgements

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with its full-featured Java Profiler.

YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/index.jsp)
and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/index.jsp), innovative and intelligent tools for profiling Java and .NET applications.
