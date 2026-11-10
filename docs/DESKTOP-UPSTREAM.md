# Luna | Modified #377 Client

This is the official #377 client that connects to the [Luna game server](https://github.com/luna-rs/luna).

![](./data/media.png)

# Setup

Please see the [wiki](https://github.com/luna-rs/luna/wiki) for a full setup guide if you're looking to create your own world.

Build your development client with Java 21 and an explicit compatible cache:
`./gradlew jar verifyClientJar -PcacheZip=/path/to/cache.zip`.
Configure your server in `client.properties`; the default is localhost.

# Compatibility
Care was made not to make any changes to the #377 protocol.

You can still use a non-luna based #377 server with this client, and any faithful #377 client can connect
to Luna.
