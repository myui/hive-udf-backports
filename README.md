[![Version](http://img.shields.io/:version-0.0.1-read.svg)](https://oss.sonatype.org/content/repositories/releases/io/github/myui/hive-udf-backports/) [![License](http://img.shields.io/:license-Apache_v2-blue.svg)](https://github.com/apache/incubator-hivemall/blob/master/LICENSE)
[![Hive](https://img.shields.io/badge/hive-v2.1.1-brightgreen.svg)](https://github.com/apache/hive/tree/rel/release-2.1.1/ql/src/java/org/apache/hadoop/hive/ql/udf)

Backports of [Hive 2.1.1 UDF functions](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF) to Hive 0.13.0.

Find the list of backported functions in [this DDL](https://github.com/myui/hive-udf-backports/blob/master/resources/define-functions.hive).

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

