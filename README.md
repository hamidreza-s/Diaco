[![Build Status](https://travis-ci.org/hamidreza-s/Diaco.svg?branch=master)](https://travis-ci.org/hamidreza-s/Diaco)

# Diaco

Diaco is a distributed actor framework for Java. It simplifies concurrent programming
aiming to be a lightweight and fast solution. Its design is heavily influenced by
[Erlang/OTP](https://en.wikipedia.org/wiki/Erlang_\(programming_language\)).

## Quick Start

Diaco can be used as a dependency of an existing project. In this case the [Jitpack](https://jitpack.io/) repository 
must be added to the `pom.xml` file of the target project to use the Github releases. Then, by specifying Diaco 
dependency with intended version we are ready to go.

```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>
...
<dependency>
  <groupId>com.github.hamidreza-s</groupId>
  <artifactId>diaco</artifactId>
  <version>0.1.0</version>
</dependency>
```

The following code snippet creates an **Echo** actor which echoes back whatever message tag it receives.

```java
Config config = Config.newConfig();
config.setProperty(Config.SCHEDULER_RUN_QUEUE_SIZE, "1024");
config.setProperty(Config.SCHEDULER_THREAD_POOL_SIZE, "4");
Diaco diaco = Diaco.newInstance(config); // 1

Actor<Object> actorEchoing = new BaseActor<Object>() { // 2
    @Override
    public State<Object> receive(Message message, State<Object> state) { // 3
        Reference senderActor = Reference.fromString(message.getFrom()); // 4
        String senderTag = message.getTag();
        send(senderActor, new Message.Builder().tag(senderTag).build()); // 5
        return state; // 6
    }
};

final Reference actorEchoingRef = diaco.spawn(actorEchoing); // 7

diaco.spawn(new BaseActor<Object>() { // 8
    private String echo = "foo";
    @Override
    public State<Object> init() { // 9
        send(actorEchoingRef, new Message
                                  .Builder()
                                  .tag(echo)
                                  .from(this.getReference().toString())
                                  .build()); // 10
        return new State<Object>(); // 11
    }
    @Override
    public State<Object> receive(Message message, State<Object> state) { // 12
        assertEquals(message.getTag(), echo); // 13
        return state;
    }
});
```

1. A new instance of `Diaco` is created with run-queue size of `1024` and thread pool size of `4`.
2. An actor is defined specifying `Object` as the type of its state.
3. The `receive` callback of it is overridden to wait for incoming messages.
4. A `Reference` is created by using the string reference of message sender.
5. A `Message` is sent back containing the same tag content of the received message.
6. The `receive` callback must return the new `State<StateType>` of the actor.
7. The defined echoing actor is spawned and its `Reference` is stored to be used by the second actor.
8. The second actor is defined and spawned at the same line.
9. The `init` callback of it is overridden which is the starting point of each actor execution.
10. A message containing a tag and the sender reference is sent to echoing actor.
11. The `init` callback of it is responsible for initializing the actor state and then returing it.
12. The `receive` callback of it is waiting to receive the message which it has just sent.
13. It asserts that the receiving message tag is the same as what it has just sent.

## Concepts

There are few concepts which explain how Diaco works behind the scene such as
scheduling, actor spawning, message passing, and node communication in 
distribution mode.

### Scheduler

Diaco uses co-operative scheduling based on actor priority and reduction.
There is a run-queue which is responsible for storing actors based on
their priority. The main scheduler picks ready to execute actors and passes
them to its worker pool. Following diagram shows a part of an actor life cycle
from being spawned and initiating to receiving a message.

```
       (1)
diaco.spawn(actorX) +--> (2)
                        Queue
                          +
                          |
+-----------+  (6)  +-----v-----+  (3)  +-----------+
|           | Queue |           | Take  |           |
| +-------+ |       | +-------+ |       |           |
| |Actor 1| +-------> |Actor 1| <-------+           |
| +-------+ |       | +-------+ |       |           |
| |Actor 2| |       | |Actor 2| |       |  Master   |
| +-------+ |       | +-------+ |       | Scheduler |
| |-------| |       | |-------| |       |  Thread   |
| +-------+ |       | +-------+ |       |           |
| |Actor N| |       | |Actor N| |       |           |
| +-------+ |       | +-------+ |       |           |
|           |       |           |       |           |
| Wait List |       | Run Queue |       |           |
|           |       |           |       |           |
+-----^-----+       +-----------+       +-----+-----+
      |                                       |
      | (5) Add                      (4) Exec |
      |                                       |
  +---+---------------------------------------v---+
  |        Scheduler Worker Thread Pool           |
  +--------------+--------------------------------+
  |              |              |--|              |
  |   Worker 1   |   Worker 2   |--|   Worker N   |
  |              |              |--|              |
  +--------------+--------------------------------+

```

- **Step (1)**: Using `spawn` method of `diaco` instance, an actor can be spawned.
- **Step (2)**: Spawning an actor asynchronously puts it in the run-queue waiting to be taken by master scheduler.
- **Step (3)**: Master scheduler takes actors as soon as they appear in the run-queue and returns a reference immediately to method caller.
- **Step (4)**: The spawned actor then is given to scheduler worker pool to be executed by calling its `init` method.
- **Step (5)**: When the actor has nothing to do but waiting to receive a message, it will be removed from worker pool and added to a wait list.
- **Step (6)**: When a message is sent to that waiting actor from another running actor, it will be queue again in the run-queue going back to **Step (3)**. 

### Actor

An actor is simply an object which implemented [Runnable](https://docs.oracle.com/javase/7/docs/api/java/lang/Runnable.html)
interface, so it can be executed by a thread. It has *state*, *status*, and also a *mailbox* for receiving *messages*.
It can be *linked* or *monitored* by other actors having its *reference*. Here, each mentioned concept will be explained.

```
+-----------------+                                        +-----------------+
|    actorOne     |                                        |    actorTwo     |
+-----------------+                                        +-----------------+
| Status: RUNNING |                                        | Status: WAITING |
| Mailbox: []     |                                        | Mailbox: [msg]  |
+--------+--------+                                        +--------+--------+
         |                                                          |
+--------+--------+  referenceOne.send(referenceTwo, msg)  +--------+--------+
|  referenceOne   +---------------------------------------->  referenceTwo   |
+-----------------+                                        +-----------------+

```

- **Status**: It can be any of `STARTING`, `RUNNING`, `WAITING`, and `EXITING` depending on the current status of the actor.
- **State**: A generic field of the actor which can be initialized, used, and replaced during the actor life cycle.
- **Mailbox**: A priority-queue data structure holding all incoming messages based on their priority.
- **Reference**: An object containing an actor's identifier, name, and node name. It is used for actor communication.  
- **Linking**: Actors can be linked together. In this case all linked actor will be terminated when one of them terminates.
- **Monitoring**: Actors can monitor each other. In this case a monitoring actor gets notified when a monitored actor terminates.

An actor class can be defined by overriding three methods of `BaseActor` as follows.

```java
Reference actorReference = new BaseActor<Object>() {
    @Override
    public State<Object> init() {
        // your code here ...
        return new State<Object>();
    }
    @Override
    public State<Object> receive(Message message, State<Object> state) {
        // your code here ...  
        return state;
    }
    @Override
    public void terminate(State<Object> state) {
        // your code here...
    }
};
```

Inside the actor callbacks' scope, the following methods are accessible.

```java
public Status getStatus();
public Integer getPriority();
public Integer getReduction();
public Integer getIdentifier();
public Node getNode();
public Reference getReference();
public boolean hasNode();
public boolean isAlive();
```

### Reference

A reference is an object containing an actor's identifier, name, and node name. It is used for communicating between actors,
exiting, linking, and monitoring them with the following methods.

```java
public void exit(Reference actor, Message message);
public void exit(Reference actor);
public void exit(Reference actor);
public void link(Reference actor);
public void unlink(Reference actor);
public void monitor(Reference actor);
public void unmonitor(Reference actor);
```

### Message

A message is a serializable object which can be used for inter-actor communication containing *type*, *priority*, *tag*, 
*flag*, *from* which is sender reference, and finally a *body*. It is instantiated by a builder class which creates a
default message.

- **Type**: A message can be any of `DEFAULT`, `EXIT`, `EXITED`, `LINK`, `UNLINK`, `LINKED`, `MONITOR`, and `UNMONITOR`.
Except for default type, others are system messages which are not used directly by framework user and just can be created 
by using actor methods with the same name such as `exit`, `link`, and so on.
- **Priority**: Except for default, all other types have priority of `0` which means they are delivered sooner than 
other messages. The priority of messages affects the order of being called by receive callback. It is recommended 
not to use `0` for default messages.
- **Tag**: It is an optional `String` field which can be used for differentiating among different messages by content. The idea 
of tagging messages comes from [Erlang Programming Rules](http://www.erlang.se/doc/programming_rules.shtml#HDR19) which
here facilitates matching messages in a `switch case` statement.

```java
public State<Object> receive(Message message, State<Object> state) {
    switch(message.getTag()) { 

        case "foo":
        // handle message foo ... 
        break;
        
        case "bar":
        // handle message bar ...
        break;

        default:
        // handle unspecified messages ...
    }
}
```

- **Flag**: It is similar to the message tag except for its type which is `Integer`.
- **From**: It is an optional `String` field which can be used for storing sender string reference.
- **Body**: It is an optional `byte[]` field which can be used for sending complex messages after serializing them into
byte array. It is up to the framework user to choose a serializer such as [Protocol Buffers](https://en.wikipedia.org/wiki/Protocol_Buffers),
[Apache Thrift](https://en.wikipedia.org/wiki/Apache_Thrift), [MessagePack](https://en.wikipedia.org/wiki/MessagePack), 
or any other libraries.

The above fields can be set just by the message builder, but can be accessed by following APIs.

```java
public Type getType();
public String getFrom();
public String getTag();
public int getFlag();
public Integer getPriority();
public byte[] getBody();
```

### Node

By default Diaco is started in local mode, but by setting `NODE_NAME` of its config object it will be started in distributed mode.
In this case actors can communicate over the network just by knowing the remote actor's identifier and node name.
The API for message passing, linking, and monitoring actors is the same for both local and distributed mode.

For instance, in a machine which has `192.168.0.1` IP, the following code snippet starts a Diaco instance in distributed
mode and then spawns an actor whose identifier is `1234` waiting for a message.

```java
Config config = Config.newConfig();
config.setProperty(Config.NODE_NAME, "node-one");
config.setProperty(Config.NODE_MEMBERS, "192.168.0.2");
diaco = Diaco.newInstance(config);
Reference actorRef = new BaseActor<String>() {
    @Override
    public State<String> receive(Message message, State<String> state) {
        System.out.pringln(message.getBody()); // prints "hi!"
        return state
    }
};
diaco.spawn(actorRef);
```

Then, in the other machine which has `192.168.0.2` IP, a message will be sent to the waiting actor of node one.

```java
Config config = Config.newConfig();
config.setProperty(Config.NODE_NAME, "node-two");
config.setProperty(Config.NODE_MEMBERS, "192.168.0.1");
diacoOne = Diaco.newInstance(config);
Reference actorRef = new BaseActor<String>(){
    @Override
    public State<String> init() {
        Reference remoteActorRef = new Reference(1234, "node-two");
        send(remoteActorRef, new Message.Builder().tag("hi!").build());
        return new State<String>();
    }
};
diaco.spawn(actorRef)
```

Under the hood, Diaco is using [Hazelcast](https://hazelcast.org/) for distributed in-memory data storage and networking.

## Todo

- [ ] Implement a Shell actor to read commands from standard input then evaluates and prints them in a loop (REPL).
- [ ] Extend BaseActor to implement GenericServerActor based on [gen_server](http://erlang.org/doc/man/gen_server.html) behaviour. 
- [ ] Extend BaseActor to implement StateMachineActor based on [gen_fsm](http://erlang.org/doc/man/gen_fsm.html) behaviour.
- [ ] Extend BaseActor to implement EventHandlerActor based on [gen_event](http://erlang.org/doc/man/gen_server.html) behaviour.
- [ ] Write the Javadoc, a benchmark, and a sample application.

## Contribution

Comments and contributions are greatly appreciated.

## Name Origin

[Diaco](https://en.wikipedia.org/wiki/Deioces) was the first king of the Medes. 
Its name is derived from the Persian word *Dahyu-ka-*, meaning "the lands".

## License

The MIT License (MIT).