# Minimal demonstration of shared subscriptions issue

## Effects

- [✓] Bug exists Release Version 1.1.1 ( Master Branch)
- [?] Bug exists in Snapshot Version 1.1.2-SNAPSHOT (Develop Branch)

`HEAD` of `develop` branch appears to be broken wrt. my test program suggesting a regression wrt. `1.1.x` API current exists, which may or may not be related.

## Issue

Callbacks don't get called when subscribing to [shared subscriptions](http://www.hivemq.com/blog/mqtt-client-load-balancing-with-shared-subscriptions/), e.g. `$share/group/topic`, using the `subscribe(String topicFilter, int qos, IMqttMessageListener messageListener)`API [(code)](https://github.com/codeasone/shared-subscriptions-issue/blob/master/src/main/java/io/github/codeasone/SharedSubscriptionsIssue.java#L49-L50).

However, shared subscriptions work as expected when using the `setCallback(...)` [(code)](https://github.com/codeasone/shared-subscriptions-issue/blob/master/src/main/java/io/github/codeasone/SharedSubscriptionsIssue.java#L43-L46) API before invoking `subscribe(String topicFilter, int qos)`.

This issue impacts downstream wrappers such as the Clojure library [machine-head](https://github.com/clojurewerkz/machine_head), which is what led me here...

## Instructions

- Run a local MQTT broker `localhost:1883` that supports [shared subscriptions](http://www.hivemq.com/blog/mqtt-client-load-balancing-with-shared-subscriptions/) e.g. HiveMQ

In the root of this project:

- `make build`
- `make run`

## Expected

```
Connecting to broker: tcp://localhost:1883
Published: 1
Received on [my/topic]: [1] for client: [subcriber1]
Received on [my/topic]: [2] for client: [subcriber2]
Published: 2
Published: 3
Received on [my/topic]: [3] for client: [subcriber1]
Published: 4
Received on [my/topic]: [4] for client: [subcriber2]
Published: 5
Received on [my/topic]: [5] for client: [subcriber1]
Published: 6
Received on [my/topic]: [6] for client: [subcriber2]
Published: 7
Received on [my/topic]: [7] for client: [subcriber1]
Published: 8
Received on [my/topic]: [8] for client: [subcriber2]
Disconnected
All done.
```

It's clear that the messages are load-balanced in a round-robin fashion across the two subscribers.

8 messages received in total.

For completeness, compare with normal subscription behaviour:

```
Connecting to broker: tcp://localhost:1883
Received on [my/topic]: [1] for client: [subcriber2]
Published: 1
Received on [my/topic]: [1] for client: [subcriber1]
Published: 2
Received on [my/topic]: [2] for client: [subcriber1]
Received on [my/topic]: [2] for client: [subcriber2]
Published: 3
Received on [my/topic]: [3] for client: [subcriber1]
Received on [my/topic]: [3] for client: [subcriber2]
Published: 4
Received on [my/topic]: [4] for client: [subcriber1]
Received on [my/topic]: [4] for client: [subcriber2]
Received on [my/topic]: [5] for client: [subcriber1]
Published: 5
Received on [my/topic]: [5] for client: [subcriber2]
Published: 6
Received on [my/topic]: [6] for client: [subcriber1]
Received on [my/topic]: [6] for client: [subcriber2]
Received on [my/topic]: [7] for client: [subcriber1]
Published: 7
Received on [my/topic]: [7] for client: [subcriber2]
Published: 8
Received on [my/topic]: [8] for client: [subcriber2]
Received on [my/topic]: [8] for client: [subcriber1]
Disconnected
All done.
```

16 messages received in total.

## Received when executing BAD path

No messages arrive. Published messages are reported as unhandled.

```
Connecting to broker: tcp://localhost:1883
Published: 1
Published: 2
Published: 3
Published: 4
Published: 5
Published: 6
Published: 7
Published: 8
Disconnected
All done.
```
