# Theia API

## Grobid

To use the **lightweight** model (CRF)

```shell
docker run --rm --init --ulimit core=0 -p 8070:8070 lfoppiano/grobid:latest-crf
```

## Running

#### Tests

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
