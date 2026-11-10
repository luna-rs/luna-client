# Luna | Modified #377 Client

This desktop overview is retained from the earlier client README. For current
desktop and browser instructions, use the [main README](../README.md).

The client targets revision 377 and defaults to your own local
[Luna game server](https://github.com/luna-rs/luna) at `127.0.0.1:43594`.

# Setup

Use Java 21 and run a compatible server separately. Configure its address and
matching public RSA values in the root `client.properties`, then build with your
own compatible cache ZIP:

```sh
./gradlew jar verifyClientJar -PcacheZip=/path/to/cache.zip
java -jar build/libs/luna-dev.jar
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`. The build
requires the six core cache stores described in the [main README](../README.md#supply-a-cache);
it does not download a cache or connect to a hosted game automatically.

# Compatibility
Other revision 377 servers need compatible login encryption, packet handling and
cache data. Verify login and gameplay against your chosen server.
