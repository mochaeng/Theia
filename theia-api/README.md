# Theia API

## Running

##### Tests

```shell
./gradlew test
```

##### Static analyse (SpotBugs)

Main code:

```shell
./gradlew spotbugsMain
```

Test code: 

```shell
./gradlew spotbugsTest
```

##### Code formatter

First install `java-prettier`

```shell
./gradlew spotlessApply
```

```shell
./gradlew spotlessCheck
```
