Spark.io Alarm Clock
====================

An alarm clock with a mood light based on Spark Core and Spark Button.

Implementations
---------------

 - The Spark.io firmware can be found in the `firmware` directory.
 - An Android app can be found in the `android` directory.

Protocol design
---------------

UDP is used over IP, the base port is 42620 (the meaning of life + 620 for
default time), discovery happens on 42621.

### Discovery ###

The app can only be used on the local network (LAN), the app sends a packet
on the discovery port (42621) to the broadcast address, and waits for three
seconds for at most 30 replies. Replies must be sent to the same port and
must contain an ASCII encoded nick name as the sole payload. In case of
Spark.io devices, this is the unique ID.

### Setting the alarm ###

An alarm contains two properties, a 24-bit color in the RGB space, and the
time of the alarm with minute precision. It is up to the device to interpret
the time of the alarm (default on Spark Core is UTC). The packet is sent to
the base port (42620) and contains a five byte payload:

 - red component (0-255)
 - green component (0-255)
 - blue component (0-255)
 - hour of alarm (0-23)
 - minute of alarm (0-59)

The reply must be a packet with the ASCII encoded `ACK` as its payload on
the same port, the Android app tries to send the set alarm packet 4 times,
with 500 ms timeout.

Dependencies
------------

The AmbilWarna color picker (see below) is included in the source tree,
you'll need to get the Spark Button driver from [its GitHub repository][1].

License
-------

The whole project is available under MIT license, see `LICENSE.txt`.

The color picker dialog is [AmbilWarna by Pascal Cans and Justin Warner][2]
available under Apache License 2.0.

  [1]: https://github.com/jenesaisdiq/SparkButton
  [2]: https://code.google.com/p/android-color-picker/
