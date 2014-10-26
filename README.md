FlowControl
===========

Useful library to implement flow control in any scope.

If you want to limit the incoming objects rate, you can use the dispatchers provided by the library to get an maximum output rate of your decision, protecting this way the backend systems that must process the objects.

There are three types of dispatchers:

  * **Default Distpatcher**: which dispences objects at fixed rate in permanent regime if the queue always have elements.
  * **Gaussian Dispatcher**: which is like the default one, but try to disperse the delivery so that there is no sinchronization effects.
  * **Quota Dispatcher**: which implements the the typical contract in which you are permitted to push N objects in a defined period. After that, the objects are dropped. 

There are listeners for capturing loss events and also for managing the quota dispatcher.

The library is very generic, so it is objects what travel through dispatchers. Whatever object. For sure, the primary application will be objects representing network packets, but it could be whatever.

Completing the library, there are several useful classes for classifying and aggregating objects from and to dispatchers. Also, connectors for connecting dispatcher systems.

There examples in source.

![Dispatchers synopsis](Dispatchers.png?raw=true "Dispatchers synopsis")
