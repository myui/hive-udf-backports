/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.udf;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.io.DateWritable;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.hive.serde2.objectinspector.ConstantObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils.PrimitiveGrouping;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

public final class BackportUtils {

    private static final String[] ORDINAL_SUFFIXES = new String[] {"th", "st", "nd", "rd", "th",
            "th", "th", "th", "th", "th"};

    private BackportUtils() {}

    // -----------------------------------------------------------------------------
    // ported form org.apache.hive.common.util.DateUtils

    private static final ThreadLocal<SimpleDateFormat> dateFormatLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public static SimpleDateFormat getDateFormat() {
        return dateFormatLocal.get();
    }

    // -----------------------------------------------------------------------------
    // ported form GenericUDF()

    public static void checkArgsSize(ObjectInspector[] arguments, int min, int max)
            throws UDFArgumentLengthException {
        if (arguments.length < min || arguments.length > max) {
            StringBuilder sb = new StringBuilder();
            sb.append("_FUNC_ requires ");
            if (min == max) {
                sb.append(min);
            } else {
                sb.append(min).append("..").append(max);
            }
            sb.append(" argument(s), got ");
            sb.append(arguments.length);
            throw new UDFArgumentLengthException(sb.toString());
        }
    }

    public static void checkArgPrimitive(ObjectInspector[] arguments, int i)
            throws UDFArgumentTypeException {
        ObjectInspector.Category oiCat = arguments[i].getCategory();
        if (oiCat != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(i, "_FUNC_ only takes primitive types as "
                    + getArgOrder(i) + " argument, got " + oiCat);
        }
    }

    public static void checkArgGroups(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, PrimitiveGrouping... grps)
            throws UDFArgumentTypeException {
        PrimitiveCategory inputType = ((PrimitiveObjectInspector) arguments[i]).getPrimitiveCategory();
        for (PrimitiveGrouping grp : grps) {
            if (PrimitiveObjectInspectorUtils.getPrimitiveGrouping(inputType) == grp) {
                inputTypes[i] = inputType;
                return;
            }
        }
        // build error message
        StringBuilder sb = new StringBuilder();
        sb.append("_FUNC_ only takes ");
        sb.append(grps[0]);
        for (int j = 1; j < grps.length; j++) {
            sb.append(", ");
            sb.append(grps[j]);
        }
        sb.append(" types as ");
        sb.append(getArgOrder(i));
        sb.append(" argument, got ");
        sb.append(inputType);
        throw new UDFArgumentTypeException(i, sb.toString());
    }

    public static void obtainStringConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();

        Converter converter = ObjectInspectorConverters.getConverter(arguments[i],
            PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        converters[i] = converter;
        inputTypes[i] = inputType;
    }

    public static void obtainIntConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();
        switch (inputType) {
            case BYTE:
            case SHORT:
            case INT:
            case VOID:
                break;
            default:
                throw new UDFArgumentTypeException(i, "_FUNC_ only takes INT/SHORT/BYTE types as "
                        + getArgOrder(i) + " argument, got " + inputType);
        }

        Converter converter = ObjectInspectorConverters.getConverter(arguments[i],
            PrimitiveObjectInspectorFactory.writableIntObjectInspector);
        converters[i] = converter;
        inputTypes[i] = inputType;
    }

    public static void obtainLongConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();
        switch (inputType) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                break;
            default:
                throw new UDFArgumentTypeException(i,
                    "_FUNC_ only takes LONG/INT/SHORT/BYTE types as " + getArgOrder(i)
                            + " argument, got " + inputType);
        }

        Converter converter = ObjectInspectorConverters.getConverter(arguments[i],
            PrimitiveObjectInspectorFactory.writableIntObjectInspector);
        converters[i] = converter;
        inputTypes[i] = inputType;
    }

    public static void obtainDoubleConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();
        Converter converter = ObjectInspectorConverters.getConverter(arguments[i],
            PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);
        converters[i] = converter;
        inputTypes[i] = inputType;
    }

    public static void obtainDateConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();
        ObjectInspector outOi;
        switch (inputType) {
            case STRING:
            case VARCHAR:
            case CHAR:
                outOi = PrimitiveObjectInspectorFactory.writableStringObjectInspector;
                break;
            case TIMESTAMP:
            case DATE:
            case VOID:
                outOi = PrimitiveObjectInspectorFactory.writableDateObjectInspector;
                break;
            default:
                throw new UDFArgumentTypeException(i,
                    "_FUNC_ only takes STRING_GROUP or DATE_GROUP types as " + getArgOrder(i)
                            + " argument, got " + inputType);
        }
        converters[i] = ObjectInspectorConverters.getConverter(inOi, outOi);
        inputTypes[i] = inputType;
    }

    public static void obtainTimestampConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();
        ObjectInspector outOi;
        switch (inputType) {
            case STRING:
            case VARCHAR:
            case CHAR:
            case TIMESTAMP:
            case DATE:
                break;
            default:
                throw new UDFArgumentTypeException(i,
                    "_FUNC_ only takes STRING_GROUP or DATE_GROUP types as " + getArgOrder(i)
                            + " argument, got " + inputType);
        }
        outOi = PrimitiveObjectInspectorFactory.writableTimestampObjectInspector;
        converters[i] = ObjectInspectorConverters.getConverter(inOi, outOi);
        inputTypes[i] = inputType;
    }

    public static String getStandardDisplayString(String name, String[] children) {
        return getStandardDisplayString(name, children, ", ");
    }

    public static String getStandardDisplayString(String name, String[] children, String delim) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        if (children.length > 0) {
            sb.append(children[0]);
            for (int i = 1; i < children.length; i++) {
                sb.append(delim);
                sb.append(children[i]);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getStringValue(DeferredObject[] arguments, int i, Converter[] converters)
            throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        return converters[i].convert(obj).toString();
    }

    public static Integer getIntValue(DeferredObject[] arguments, int i, Converter[] converters)
            throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        int v = ((IntWritable) writableValue).get();
        return v;
    }

    public static Long getLongValue(DeferredObject[] arguments, int i, Converter[] converters)
            throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        long v = ((LongWritable) writableValue).get();
        return v;
    }

    public static Double getDoubleValue(DeferredObject[] arguments, int i, Converter[] converters)
            throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        double v = ((DoubleWritable) writableValue).get();
        return v;
    }

    public static Timestamp getTimestampValue(DeferredObject[] arguments, int i,
            Converter[] converters) throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        // if string can not be parsed converter will return null
        if (writableValue == null) {
            return null;
        }
        Timestamp ts = ((TimestampWritable) writableValue).getTimestamp();
        return ts;
    }

    public static String getConstantStringValue(ObjectInspector[] arguments, int i) {
        Object constValue = ((ConstantObjectInspector) arguments[i]).getWritableConstantValue();
        String str = constValue == null ? null : constValue.toString();
        return str;
    }

    public static Integer getConstantIntValue(ObjectInspector[] arguments, int i)
            throws UDFArgumentTypeException {
        Object constValue = ((ConstantObjectInspector) arguments[i]).getWritableConstantValue();
        if (constValue == null) {
            return null;
        }
        int v;
        if (constValue instanceof IntWritable) {
            v = ((IntWritable) constValue).get();
        } else if (constValue instanceof ShortWritable) {
            v = ((ShortWritable) constValue).get();
        } else if (constValue instanceof ByteWritable) {
            v = ((ByteWritable) constValue).get();
        } else {
            throw new UDFArgumentTypeException(i, "_FUNC_ only takes INT/SHORT/BYTE types as "
                    + getArgOrder(i) + " argument, got " + constValue.getClass());
        }
        return v;
    }

    public static String getArgOrder(int i) {
        i++;
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + ORDINAL_SUFFIXES[i % 10];
        }
    }

    // -----------------------------------------------------------------------------
    // ported from GenericUDFParamUtils()

    public static BytesWritable getBinaryValue(DeferredObject[] arguments, int i,
            Converter[] converters) throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        return (BytesWritable) writableValue;
    }

    public static Text getTextValue(DeferredObject[] arguments, int i, Converter[] converters)
            throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }
        Object writableValue = converters[i].convert(obj);
        return (Text) writableValue;
    }

    public static void obtainBinaryConverter(ObjectInspector[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws UDFArgumentTypeException {
        PrimitiveObjectInspector inOi = (PrimitiveObjectInspector) arguments[i];
        PrimitiveCategory inputType = inOi.getPrimitiveCategory();

        Converter converter = ObjectInspectorConverters.getConverter(arguments[i],
            PrimitiveObjectInspectorFactory.writableBinaryObjectInspector);
        converters[i] = converter;
        inputTypes[i] = inputType;
    }

    public static BytesWritable getConstantBytesValue(ObjectInspector[] arguments, int i) {
        Object constValue = ((ConstantObjectInspector) arguments[i]).getWritableConstantValue();
        return (BytesWritable) constValue;
    }

    public static Date getDateValue(DeferredObject[] arguments, int i,
            PrimitiveCategory[] inputTypes, Converter[] converters) throws HiveException {
        Object obj;
        if ((obj = arguments[i].get()) == null) {
            return null;
        }

        Date date;
        switch (inputTypes[i]) {
            case STRING:
            case VARCHAR:
            case CHAR:
                String dateStr = converters[i].convert(obj).toString();
                try {
                    date = getDateFormat().parse(dateStr);
                } catch (ParseException e) {
                    return null;
                }
                break;
            case TIMESTAMP:
            case DATE:
                //case TIMESTAMPTZ:
                Object writableValue = converters[i].convert(obj);
                date = ((DateWritable) writableValue).get();
                break;
            default:
                throw new UDFArgumentTypeException(0,
                    "_FUNC_ only takes STRING_GROUP and DATE_GROUP types, got " + inputTypes[i]);
        }
        return date;
    }

    public static Boolean getConstantBooleanValue(ObjectInspector[] arguments, int i)
            throws UDFArgumentTypeException {
        Object constValue = ((ConstantObjectInspector) arguments[i]).getWritableConstantValue();
        if (constValue == null) {
            return false;
        }
        if (constValue instanceof BooleanWritable) {
            return ((BooleanWritable) constValue).get();
        } else {
            throw new UDFArgumentTypeException(i, "_FUNC_ only takes BOOLEAN types as "
                    + getArgOrder(i) + " argument, got " + constValue.getClass());
        }
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

}
