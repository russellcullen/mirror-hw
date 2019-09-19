## Mirror Auth Demo

Simple app that performs signup, login, and some profile editing for Mirror service.

### Overview

`UserService` is the data service that manages user state. Clients communicate through an AIDL interface to make auth or user updates.

This service delegates all of its work to the  `UserRepository`, a business logic class that handles remote requests (through `MirrorApi` dependency) and/or cached data (with `UserStore`). Encapsulating business logic into a separate `UserRepository` class allows for much easier unit testing. See `UserRepositoryTest` and below.

### Tests

Run tests with gradle:
```
./gradlew testDebugUnitTest
```
