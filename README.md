[![Version](http://img.shields.io/:version-0.0.2-read.svg)](https://oss.sonatype.org/content/repositories/releases/io/github/myui/hive-udf-backports/) [![License](http://img.shields.io/:license-Apache_v2-blue.svg)](https://github.com/apache/incubator-hivemall/blob/master/LICENSE)
[![Hive](https://img.shields.io/badge/hive-v2.2.0-brightgreen.svg)](https://github.com/apache/hive/tree/rel/release-2.2.0/ql/src/java/org/apache/hadoop/hive/ql/udf)

Backports of [Hive 2.2.0 UDF functions](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF) to Hive 0.13.0.

Find the list of backported functions in [this DDL](https://github.com/myui/hive-udf-backports/blob/master/resources/create-functions.hive).

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
source resources/create-functions.hive;
```

## Maven repository

```xml
<dependency>
    <groupId>io.github.myui</groupId>
    <artifactId>hive-udf-backports</artifactId>
    <version>0.0.2</version>
</dependency>
```

## List of Functions

```sql
-- since v1.1.0
greatest
least
add_months
last_day
initcap

-- since v1.2.0
factorial
cbrt
shiftleft
shiftright
shiftrightunsigned
current_date
current_timestamp
next_day
trunc
months_between
date_format
levenshtein
soundex

-- since v1.3.0
quarter
chr
replace
substring_index
md5
sha1
crc32
sha2
aes_encrypt
aes_decrypt

-- since v2.1.0
mask
mask_first_n
mask_last_n
mask_show_first_n
mask_show_last_n
mask_hash

-- since v2.2.0
nullif
character_length
regr_avgx
regr_avgy
regr_count
regr_intercept
regr_r2
regr_slope
regr_sxx
regr_sxy
regr_syy
```
