# Setup

You need a working Java toolchain and your coding agent.

## 1. Java and Maven

- Install a JDK, version 21 or newer. Check with `java --version`.
- Install Maven 3.8 or newer. Check with `mvn --version`.

## 2. Get the repo

Clone your fork and work inside it. Commit as you go. Milestone 1 asks you to
write a prediction before you run your race test. A commit is the cheapest way
to show a TA that you wrote it first.

## 3. Build and run the tests

From this directory:

```
mvn -B test
```

## 4. What green looks like

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The suite prints the service's log lines as it goes, one per booking event.
That noise is normal. Learn the shape of a line now, since you will read these
later:

```
2026-11-02T14:31:07.412Z INFO booking.confirmed booking=bk-1 room=WEH-5409 start=540 end=600
```

If the build is red before you have changed anything, fix your environment
first.

## 5. Running the stress runner

The stress runner is a main class. It is not in the suite and CI never runs it,
so start it yourself:

```
mvn -q compile
java -cp target/classes edu.cmu.cs214.lab10.harness.StressRunner
```

It takes an optional round count: `... StressRunner 2000`. Expect the report to
differ from run to run.

## 6. Editor

Use any editor or IDE you like. VS Code, IntelliJ IDEA, and Eclipse all import
a Maven project directly. Open this folder, the one with `pom.xml`.

## 7. Your coding agent

Configure your agent and point it at this folder. It can do the
reading, the test writing, and the code changes. At recitation, be ready to
explain the judgment calls in `RACE.md`.
