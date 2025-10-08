## Grobid

To use the **lightweight** model (CRF)

```shell
docker run --rm --init --ulimit core=0 -p 8070:8070 lfoppiano/grobid:latest-crf
```

## ClamAV

```shell
docker run -p 3310:3310 clamav/clamav:stable
```

testing virus with EICAR file:

```
echo 'X5O!P%@AP[4\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*' > eicar.com
clamdscan --fdpass eicar.com
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
