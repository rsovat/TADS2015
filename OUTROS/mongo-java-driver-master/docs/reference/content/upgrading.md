+++
date = "2015-03-19T12:53:39-04:00"
title = "Upgrade Considerations"
[menu.main]
  identifier = "Upgrading to 3.6"
  weight = 80
  pre = "<i class='fa fa-level-up'></i>"
+++

## Upgrading from 3.5.x

The 3.6 release is binary and source compatible with the 3.5 release, except for methods that have been added to interfaces that
have been marked as unstable, and changes to classes or interfaces that have been marked as internal or annotated as Beta.

## Upgrading from 2.x

You must upgrade first to 3.0 driver.  See the Upgrade guide in the 3.0 driver reference documentation.

### System Requirements

The minimum JVM is now Java 6: however, specific features require Java 7:

- SSL support requires Java 7 in order to perform host name verification, which is enabled by default.  See
[SSL]({{< relref "driver/tutorials/ssl.md" >}}) for details on how to disable host name verification.
- The asynchronous API requires Java 7, as by default it relies on
[`AsynchronousSocketChannel`](http://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousSocketChannel.html) for
its implementation.  See [Async]({{< ref "driver-async/index.md" >}}) for details on configuring the driver to use [Netty](http://netty.io/) instead.

## Compatibility

The following table specifies the compatibility of the MongoDB Java driver for use with a specific version of MongoDB.

|Java Driver Version|MongoDB 2.6|MongoDB 3.0 |MongoDB 3.2|MongoDB 3.4|MongoDB 3.6|
|-------------------|-----------|------------|-----------|-----------|-----------|
|Version 3.6        |  ✓  |  ✓  |  ✓  |  ✓  |  ✓  |
|Version 3.5        |  ✓  |  ✓  |  ✓  |  ✓  |     |
|Version 3.4        |  ✓  |  ✓  |  ✓  |  ✓  |     |
|Version 3.3        |  ✓  |  ✓  |  ✓  |     |     |
|Version 3.2        |  ✓  |  ✓  |  ✓  |     |     |
|Version 3.1        |  ✓  |  ✓  |     |     |     |
|Version 3.0        |  ✓  |  ✓  |     |     |     |

The following table specifies the compatibility of the MongoDB Java driver for use with a specific version of Java.

|Java Driver Version|Java 5 | Java 6 | Java 7 | Java 8 |
|-------------------|-------|--------|--------|--------|
|Version 3.6        |     | ✓ | ✓ | ✓ |
|Version 3.5        |     | ✓ | ✓ | ✓ |
|Version 3.4        |     | ✓ | ✓ | ✓ |
|Version 3.3        |     | ✓ | ✓ | ✓ |
|Version 3.2        |     | ✓ | ✓ | ✓ |
|Version 3.1        |     | ✓ | ✓ | ✓ |
|Version 3.0        |     | ✓ | ✓ | ✓ |
