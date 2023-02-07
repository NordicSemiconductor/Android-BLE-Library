# Introduction
This application serves as a comprehensive test of the BleManager's capabilities, utilizing various callbacks to demonstrate its full range of features and usage.


## Features
* Connection

The _BleServerManager_ is responsible to open the Gatt server and initializing the services. 
The server advertises the services through GATT server using a randomly generated Service UUID, making them available to remote scanning devices.
The client, in turn, scans for an advertising device with the same Service UUID and establishes the connection with the one it discovers first. 
Upon establishing the connection, both the client and the server begin interacting with each other. 
The _BleManager_ is responsible for managing the connection and communication between the peripheral device and the client. 
* Requests

The application includes an example implementation for different requests such as Write requests, read request, 
atomic request queue, reliable write request. The implementation for notification and indication callbacks are also included.

* Splitter and Merger Functions

  The application contains three different splitter and merger functions.
    * Header based splitter and merger

      The data is split into packets and sent with a header indicating the size of the message.
      The first packet contains the header and part of the message, while subsequent packets contain the remaining parts of the message.
      The packets are reassembled in the same order, with the header being used to determine the expected size of the message.
      The received message bytes are merged until the size matches the expected size specified in the header. 
    * Flag based splitter and merger
  
      The data is split into packets and each packet is marked with a flag indicating whether it is the full message,
      the beginning of a message, a continuation of a message, or the end of a message.
      The packets are reassembled based on the flags and the message bytes are merged to form the complete message.
    * Mtu based splitter and merger

      The data is split using the default mtu splitter.
      Before splitting, it checks if the size of the last packet is less than the maximum length specified for a single write operation.
      If it is, a single space character is added to the end of the message. When merging the data, it continues to merge packets
      until the message size is less than the maximum length.



* Callbacks
  
The application includes different write request callbacks such as add, then, before, after, trigger, done, and fail as a sample use case. 
Similarly, different value changed callbacks such as with, filter, filterPacket, and merge have also been implemented.

## Requirements
The application depends on the Android BLE Library. 
* It needs two Android 4.3 or newer are required in which one acts as a server and another as a client. 
### Required Permissions
On Android 6 - 11, the app requests for Location Permission and Location services 
to provide accurate Bluetooth LE scan and advertisement results.
However, with the introduction of new [Bluetooth permissions](https://developer.android.com/guide/topics/connectivity/bluetooth/permissions)
on Android 12 and above, the BLUETOOTH_SCAN permission can be obtained by including the parameter
```usesPermissionFlags="neverForLocation"```. 
This exclusion of location related data from the scan results eliminates the need for requesting Location Permission.