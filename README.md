FlowControl
===========

Useful library to implement flow control in any scope.

If you want to limit the incoming objects rate, you can use the dispatchers provided by the library to get an maximum output rate of your decision, protecting this way the backend systems that must process the objects.

The library is very generic, so it is objects what travel through dispatchers. Whatever object. For sure, the primary application will be objects representing network packets, but it could be whatever.

Completing the library, there are several useful classes for classifying and aggregating objects from and to dispatchers. Also, connectors for connecting dispatcher systems.

There two simple examples in source.

![Dispatchers synopsis](Dispatchers.png?raw=true "Dispatchers synopsis")
