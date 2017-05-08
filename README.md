[![Version](http://img.shields.io/:version-0.0.1-read.svg)](https://oss.sonatype.org/content/repositories/releases/io/github/myui/hive-udf-backports/) [![License](http://img.shields.io/:license-Apache_v2-blue.svg)](https://github.com/apache/incubator-hivemall/blob/master/LICENSE)

Backports of [Hive 2.1.1 UDF functions](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF) to Hive 0.13.0

## Usage

```sh
-- build jars
$ bin/build.sh

-- run Hive cli session
$ hive
```

```sql
-- deploy jar
add jar target/hive-udf-backports-x.y.z.jar;

-- load functions
source resources/define-functions.hive;
```

## Maven repository

```xml
<dependency>
    <groupId>io.github.myui</groupId>
    <artifactId>hive-udf-backports</artifactId>
    <version>0.0.1</version>
</dependency>
```

