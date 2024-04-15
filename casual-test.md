# Casual Test Tool

The purpose of the casual test tool library is to provide a simple/light weight mechanism or embedded instance of
casual-java such that developers can test their integrations.

Initial focus of the tool library is to provide a embedded event server/ simulator which publishes metric events.

The tools provided will as much as possible utilise existing code and components of the casual-java, though provide 
additional test apis to allow for example, test data and fault injection.

It must be light-weight and fast such that initialisation can be perform often within for example unit tests without
significant overhead.

