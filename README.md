# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5TAek8wAvP4DAoODgzG8YACUUeyRVCzkkCDQwnxC09IDMeygIAFdsAGIAFgBmAA4AJjcQGALkewALMB0EXMMomLioBKTMGHhkcxgAWgA+ckoaKAAuGABtAAUAeTIAFQBdGE9cgygAHTQAbxgAIh3KNGAAWxQT6ZOTgBpT3HUAd2gOO4fnk5Qr4BIBDfE4wAC+fQm1EoI3GbE43BmkTyYEMUBQAEc2nFIfCuDCxlCqKJZujOqioAAKc5QWTyACUpn6OnRwAA1jAAELADgwdFYtRYfr9PGIkaE4RTWbcjhRAVxACiAA8VNgCL1hUSCYTEKgMLMik4nAdjicbupgPZbrMTgqoDkkXpefzsWBQRD+ugOEyiaJYTJtEoVOpZlawABVXbU3aXG6M-p0xTKNSqf3GWYAMSQnBgkcoiZgOksMBpsbEkOACGYib0BhgGcBrXRW3gSTQKHAiTQMAAMhB1JD+sZxeNE0GU7MyABFHvK1Xq7s+aScmB2h2DgPycfqEe+lSzMfJ9RzlBqrsbyV+4aE0WUA+Bo+qE9njXC2+wa-jSV3mAAViNF6TNq4y6uYsz-saRynOaqiWtapxrtAsyUhwaggFAxALjAEAAGYwJQDr0u6PooAgdYAHIQEY2i1oYOGNrk6IbpWzB5rAbxIGADQljG1yGJWrIcMWKBKrEYAaJqCYPsGqafnuKCzGxiYbv0l4qLu76zAoCCCZYqzsugz4LiprDsPiH4SkBSKGiUJnfhZIGDPqMA2Sa0EpnBdwIfaSEwAJKA8sW8hsugxEbqRdZsWWMCxH5VCNro3AmYeMm7mpCkwGguQIAgdmTFeo7SROMDoQFqJsdG+baPGkkpSmaY6FpHC8kp2h5SI6mEnVIYlayqIKLkXGUsAg0NImNWat1snjOmMAKM1c2jcpknyRpZmIlpS1tSt75pVZk6UEglZIAAXoYA1cctkn2buoHORURpuWcvE3CCvwjVxqwQCFaAguC4WcJCXpmIDKR+BkENBGDfYxMk4SQwj-hZDk+QFNA7BWrUGZwAq0hwAovYQLDkJ3cwlnQkiiwrBsWwGOoXZPaWfFvS8+iqB8UBfPcxGqVZ-qacig1opirq4utBJfvl+4wAgRPZpVtLVT6LIBRyMp8iLgobrtcn2dKPJyq6RnnpqN1yaTBqPVBZoeVaXm2j5jo8hr8puv9nqg7zHWGF1RU9WGFVM3GkJTQ1mbZi1uwFkWPEXHxPosZuuj6IYDZAoxhg+HAbYdlhfYDpqw5yVNk4zsbSQtsuq6Oxuoe61LGVTeXySmw3a0Ij+Tcqqexmajr5NSn+AGtxTt1OWA4FW6aMGeTaiFIihaEYS+3a4fhjtEf9QNkYYlHUfItHr+uk1+7JA8krmUfaC2WU5T6-SJwAkmg8XILyQeGNAMCvOznztcSnUbziyROGS4o1oCnRQBwZuJl+6SwprMeeGsbBJAMP-YCAw9QTxck4AAjE9Geds56O1mLkMBXEIFnS+FvTUEVDAADVjq8lKqhDAR0yL-wKknbcqhZilTkCgC6DRhpbQZMlU+Yc5oLSEVdEeACfZAI7kiGR21JJwK1EiYQ7DIHIOwKgsQK0zY6nHrMB6kFTQfxZicD6DQvo-T+h6YUwMPbemBmDRGCNoZExRO4jxENkZ5EKNkFA6Baj1CaME0JMMfH9FJv6PW8xpAKlnKsBUmxPB01UAzchtjvroEhGbOEwDZgRBRMLV2YslHxIbrMWW9gUQiM+nktAjIClt19luR8oYUBgCEY03JP0ap1xmo1esEdFqXWvjHSAP0E5ViTofNOTZM6tjQO2TsFd84SWFEXDpSYZKl1nN3FelcVzz1rhI+u3t7ydJkjAuRV5FHmRufslM9y3zAL2ggoeThAKj3NiYn5BDbbwQdg6ZCqFVDoUwl2bCeECLQE3hCbeFEqJaAPinI+0BmLzNATYyhUDxG3Pqlc+Rsxb65UMe0p5G1cw5IJdA45vc1GfNJT+Q0uD0EOUwWBHB+Cjg2wtEQ7y4KSz0owlQk4yLaE7xgIwt+EyBn5JWnXc+0sVHyE4Z1QqxKeqoW4P1Ua-S7HoAmsKYZRhRnSFIj086ojgBaoUUUpRk5ciPlgaygeP4zFcrHlg0xU9HEuKBoDNx8M-EZGhmJGAABxPiGhw0RrSAE1G6JeQFGwNmDkPZo1xvNCTce1TvlzBjQqGmnh7B8QODYk1LcNH82KYLVELtRYilZfAslMsxJ5rUMa5prSvbyP9CXGAYY+k1v7SHS5Izw45g1cAQsQVmlzOrDRTFSyM4tmzms3OsKtkbl2Tq15PVpxHPnLCpcZya4n11WfDtF8u7ntfIOx5zrnncMfO8ttVS2VIggn8qYfreUQWBUK0F88IVLxhRXNeCKoBIpIrKve6Lk51jgzi1i4rIHehVZctVGUKWOvbu+vF4CJVQK-R8n9XrrJ4N9QC-1fKTSCtgsKsFvkyH4vI18aVTjZXyqQLyIRtaiXHrvfJTakzNVUu9sO0+3SwA9tUJSM1Ulb2SKzDmHtsjhTpWI7SnNcQAA82ntCjA9dRjtP4tHHTOro-R9HjGMbMU9St5ovJzEOCcNzKBH7SC8rgioJQii-DeA0TiKAorM25r8FooA2RRdejF04PnyLRYeGCTYQb8KA2DWG1ISbk3hDgH1QwPbfGFdCNkQJBQOAAHY3BOBQE4WoCoShwBxgANngKV2NfEYAFqwUWweVM1jpJ89W0ataTSpb4o4wppkXVNvKa2xb5lhsX34aiHtlItsoB7YbQUzwJ2DOVqyNWzsXRaweYAt9tKZSHcVEyk2VH1u-str8uRGCLY4PMe5MD9sIOFku5rOIPMcvejabJvZPCFPjqm5OtTYmNPjPnYumAMz8kVnmTWddDFmxZxzhs7s+7C46F3CO09zdTnV2PuavD97paPp7i9l9t21u0uZyvbWnqrN-uHrpvmDHgNT3+6x8DJCYCLyhcvLCsGN5hU9EhtFa60PXuFInUjFDuOiZ4V8zthGZNDrkgLLXDQGWUe-W9mjltOVG++4CjlzHCES9FZxsj2GpX33wvxphiqRO4fU6Si+86iMw66b1Mq+2+KUh8+NXXj5JFkEgM2Uz0nrrUruz+HzfmLPW75wdDCtnhYoLQGg+33Kfsuetjn-zsxAvBfdk43LEOQaQ8TZVwIYMABSEBsx9ZuBVzvmRquo0sKRWWbwwlIEaGAcfOUIBT97-3nttQ4sgDZINoYNv5gLHDOWibOTptHGwAgYA4+oDZ1llAZ4tf5tC6z0iUpQsW3XY5xLCTMAABWfe0A7Z-9mI9mAMdgjqdpCCrOyFyCDhUjdk6u-kiA9qDmAJRuogkoaJ9oLv8k5ryugaBuLoDpLk6K-mDk3m4mzj7EerDqOj0vDk0qdkjnroSLNJpkJvaujpjskNjquhinWBugTqsusnnP2NskOOTsXPJuQGXM9hXJerTtijesjsHkzqfJRnpibo2lzsylbmKO9j8gBg7oxiBtbC7gQaKtLtCicvLoRIrj7qivvKhnRPjgYhrrilhlQgnqlEoQRtlJShntDjSj+GbhbtIXWn3Lzhorbo5o5Ixk7gKiYcQm7m4VAl7uFL7gqsJsuoHoofhpJmNG1GHpQRHmGDtrXqph+p4TOmMlpnxMpCZInKvhwBAGoJlBAMwCJGJCZAwYnl4eSj4Z0atOoUtoga7Jbq9joTvugf0UYtETgaLixrPCKr5EQVdiQbxjKnWKvu0XECIbVAzp-mng6v0YnFfl-P3j5n5DpAFEJBjgZKEStAMQEcopcYFPpD9KMWEZZhETgrZPcZgYBsLs5K5MYSCqYb5P5IFDcfYjQvcXQq2NfnFAlC0M4b8WoYUQciWNgOitHjcNSJiQIhwAcWUQoYwZUWQMADQAPigDpkbq+vAa6u6r8doRggkj6oyfWgCdgtXocNlrQi3ryThs3gKcDPluDMPpkMVtwHgH5NgKfoQD0HDAVmKUjKPkEt0MWHUDPhEmqQMAFLAMADKcgCAPKQNrEoWjvnMEkikmklsMYFDv8Y-iUmUsQUKPARttLCAJKVSDVKieUcVB6bqSplOkHpUYgLqRjg0IYChrRCugsnjunPwduoIXusIQemIeHuiVTiETTucsSd0Tkb6ceCEQBrSQLJoazq6bof+l9pXoCkYdPCCQkb5OYbLrClYYijYbCchqroYOhrAfptZm6jJDzp8SyQLoOgYbylyY4sKZwEAA)

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
