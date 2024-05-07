# Casual Test

The purpose of the casual test module is to provide a simple/lightweight embedded instance of
casual-java such that developers can test their integrations.

Initial focus of the module is to provide an embedded event server/ simulator which publishes service call events.

The tools provided will as much as possible utilise existing code and components of the casual-java, though provide 
additional test apis to allow for example, test data and fault injection.

It must be light-weight and fast such that initialisation can be perform often within for example unit tests without
significant overhead.

It must be thread safe and allow multiple concurrent instances to run within the same jvm allowing concurrent/parallel
runs of test which utilise seperate instances of the embedded server.

