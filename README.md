# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Server Design

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5xDAaTgALdvYoALIoAIyY9lAQAK7YAMQALADMABwATG4gMHHI9r5gOgjRhgBKKPZIqhZySBBomIiopAC0AHzklDRQAFwwANoACgDyZAAqALowAPTRBlAAOmgA3gBEs5RowAC2KCvdKzArADSHuOoA7tAcewfHhyhbwEgIN4cAvpjCXTBtrOxclF6q3WUE2O1edxWZ1Ulyg132hxOKweTxeCJWHzYnG4sF+X1EvSg5UqYEoAAoyhUqpQygBHYpVACUn06oh+7Vk8iUKnUvUCYAAqnMySCwShmZzFMo1Kp2UYdN0AGJITgwIWUSUwHSWGCi7ZiHRE4AAazVcxg5yQYF8urmYpgwAQRo4OpQAA8SRpJdyZT88ayVL11VBJSyRCo-e0voCYAonShgC7Rib0ABRN0qbAEOph77NX6Ncy9BJOJKLVY7dTAQJ7Q6pqBRHq2jb6h3xxM6+TG9AYsycTDe6XqSMdcMoXpoaIIBC5tm-Qc81TdEBG0nBkVzSUS7Q+4e-YzdBQcDhmjXaWcR+c7odLlcJ0kKaLWsnAJ++LcD6+LuUHo8nx-WqG+KXr8WIAk2lIkjSahTlgYE4iO0ZNsCdr6gcvS3Eir7WqMEDdmg6GHO8uaUCOhYYL0aROE45ZrKh4IwBhiKHNhvi4fhhG3B86AcKYkQxPE0ABIYcSKnAqbSHACgwAAMhAFT1ORzD+tQMaDCMEzTAY6i1Gg5Z6jsJzQrCHAfEhcrwTGSwGSgRn6DCVyYv8CEqVQBIwAg8kqmSckKXSDJgMywGGFeXI3nyKCCsKNnbmF377gqMDKqqwaatqzagvqn5xb6+ZRgG46niG56GgmpoCpsb7QEgABeKC8cFiGdDGFWsdVdUcOmmbZvU5l5fAyBFjAJahOWhyVqo1a7Bh9aNr00SVda7X1Qc3H9o1oVSouvR3nIKAAb4L5vh+C65e0B6xsesbHeeG2gc5Ma+SqZSqLBmCWbirlWTZJysex6Bmc1n3tEplFOKNyw-Q6b7-Wga28REUSxHEkQoOg2S5PkqPo75MRYEpcpIb0fTSKmMmpqMqaTFM2mqLpix-Xh6Akbi7Qfb0jP4e9D2s6ObmBtD1rmIQe1HThTNoEFBVyqdvIwPyB1i2xEuxVtZ3yr0yX-jd8hajqnPM7Lsr9cFvQHaGpUmmqi2+MtDXSybQNBjbdtdSgWa6SzZGDRRw3g2NKwTVNtYrLN0DzS7UC1StMDw9z2Kka57mefYeM+fJeP+WogUXiFHJfjKvQcCg3APm+SsnQXe7nYl0gl5Fhjm7dDtfU2MB4V7-WgzAVFOH2CP8cjRInnE2AqqaMkkjAADi+oaATrfE9PFPU-Y+oMzDEud2zPMc5vXMfYTBV70LGAi6SSuw1LY4y1XS7y5FisG5L2Vq9XGtJSq2uAdoeuC8rXMjZNTHGbHWwBMCW3KpHaO9sb6O1Uk2VqVUo4dTdh7HMfUCw+zAMWf2yxxoymDjNBs4ddTQI6qtfu8dwJHxAQ-MAs9KxkmvvzPOMg74RQYXPZhr9dzGxrprL+bYEAz31KGIB8DWFLzEdocYucRzs1EZWahLlF4wCWGvSsxMNH6gAJLSBOOcXwVoUDBjFCcQooBjRmP1CcTRKAABy+o3jjEBgg72TQcE92ouWex98+g6J2Powxxi1z0VslqBAVibGGXlvqJxOwXFx0RgJOIHAADsbgnAoCcNkVMSQ4DiQAGzwFXIYRhhgGjYNoV0Ymwwxir3XjbWGvj4nOO3n8BOTZn6tJ2AklATkuk1PcqxYWIBRbPxYXOfOOU5YK3LpM3hN4fyJS1tdH+ut0rPyWfFJOAsm7yAgUaKBbUUH1XkZIlq5D6poJ6p3LBnjcEQyWAQqsNZiFzTIacmBlCeIqMTvlOhu1SQVLJPY-pUzLwzLfvfYFKBQXgv1KrPhKzehwDKUolA4i77AKkZioCLcd5dN6BU-5wM+ZWXsbo0y9yQbYLBjRZYVKaV-JScjSwJdPLnAxkgPIYAOXTggNygAUhAFUmLsiWJAMaKpnialqQGAKTSUx7Eb3Fvhcs2AokcqgHACAnkoB2L0dINxeYiXgRPgA9AmrtWUD1Qao1QSTVkuGQLUZZ9xkX0WRtaFfDOFP33ugZFyyEqCNVAc4Af9tkSL2YVCNRyyrW2+R1C5aikFLTOZ1DM7s7mYLpY8v2zzXmTXeXWEhTYFrJpjnHO65qcTzXCS62NvQABWYq0Cgqhlq4AOr7XQEdSgfRkK2FG04Qi41wb4oCJnpFfF54Y2ArxRUi2xzYzthdDAZMXNa0UqbHGZ0W60zZvQb1IGHiholjLPgwOhDS2h3LRzddnYUwEVjlQn17DZn31iFoEF+oNwtlid23t+r+1xKdZO9WF0BTYF-eUmRusjEmJiSgKYUrrGNp3UTduMq80DQLb3KhbL4iRB7TyvlpGdSIATLAYA2AtXn10jAWV5h5VNhJmTCmVNpjGA6abGAIBuB4AUPR5AnrPYftHUYeupJpAACEeESOnXXUujdpypsXTGDueHu6Eb+UAA

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
