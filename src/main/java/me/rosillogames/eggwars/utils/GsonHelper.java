package me.rosillogames.eggwars.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class GsonHelper
{
    private static final Gson GSON;

    public static boolean isStringValue(final JsonObject jsonObject, final String string)
    {
        return isValidPrimitive(jsonObject, string) && jsonObject.getAsJsonPrimitive(string).isString();
    }

    public static boolean isStringValue(final JsonElement jsonElement)
    {
        return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(final JsonElement jsonElement)
    {
        return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber();
    }

    public static boolean isNumberValue(final JsonObject jsonObject, final String string)
    {
        return isValidPrimitive(jsonObject, string) && jsonObject.getAsJsonPrimitive(string).isNumber();
    }

    public static boolean isBooleanValue(final JsonObject jsonObject, final String string)
    {
        return isValidPrimitive(jsonObject, string) && jsonObject.getAsJsonPrimitive(string).isBoolean();
    }

    public static boolean isArrayNode(final JsonObject jsonObject, final String string)
    {
        return isValidNode(jsonObject, string) && jsonObject.get(string).isJsonArray();
    }

    public static boolean isValidPrimitive(final JsonObject jsonObject, final String string)
    {
        return isValidNode(jsonObject, string) && jsonObject.get(string).isJsonPrimitive();
    }

    public static boolean isValidNode(final JsonObject jsonObject, final String string)
    {
        return jsonObject != null && jsonObject.get(string) != null;
    }

    public static String convertToString(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive())
        {
            return jsonElement.getAsString();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a string, was " + getType(jsonElement));
    }

    public static String getAsString(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToString(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a string");
    }

    public static String getAsString(final JsonObject jsonObject, final String string, String def)
    {
        if (jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive() && jsonObject.get(string).getAsJsonPrimitive().isString())
        {
            return jsonObject.get(string).getAsString();
        }

        return def;
    }

    public static boolean convertToBoolean(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive())
        {
            return jsonElement.getAsBoolean();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a Boolean, was " + getType(jsonElement));
    }

    public static boolean getAsBoolean(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToBoolean(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a Boolean");
    }

    public static boolean getAsBoolean(final JsonObject jsonObject, final String string, boolean def)
    {
        if (jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive() && jsonObject.get(string).getAsJsonPrimitive().isBoolean())
        {
            return jsonObject.get(string).getAsBoolean();
        }

        return def;
    }

    public static float convertToFloat(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber())
        {
            return jsonElement.getAsFloat();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a Float, was " + getType(jsonElement));
    }

    public static float getAsFloat(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToFloat(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a Float");
    }

    public static float getAsFloat(final JsonObject jsonObject, final String string, final float float3)
    {
        if (jsonObject.has(string))
        {
            return convertToFloat(jsonObject.get(string), string);
        }

        return float3;
    }

    public static long convertToLong(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber())
        {
            return jsonElement.getAsLong();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a Long, was " + getType(jsonElement));
    }

    public static long getAsLong(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToLong(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a Long");
    }

    public static long getAsLong(final JsonObject jsonObject, final String string, final long long3)
    {
        if (jsonObject.has(string))
        {
            return convertToLong(jsonObject.get(string), string);
        }

        return long3;
    }

    public static int convertToInt(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber())
        {
            return jsonElement.getAsInt();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a Int, was " + getType(jsonElement));
    }

    public static int getAsInt(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToInt(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a Int");
    }

    public static int getAsInt(final JsonObject jsonObject, final String string, final int def)
    {
        if (jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive() && jsonObject.get(string).getAsJsonPrimitive().isNumber())
        {
            return jsonObject.get(string).getAsInt();
        }

        return def;
    }

    public static byte convertToByte(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber())
        {
            return jsonElement.getAsByte();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a Byte, was " + getType(jsonElement));
    }

    public static byte getAsByte(final JsonObject jsonObject, final String string, final byte byte3)
    {
        if (jsonObject.has(string))
        {
            return convertToByte(jsonObject.get(string), string);
        }

        return byte3;
    }

    public static JsonObject convertToJsonObject(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonObject())
        {
            return jsonElement.getAsJsonObject();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a JsonObject, was " + getType(jsonElement));
    }

    public static JsonObject getAsJsonObject(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToJsonObject(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonObject");
    }

    public static JsonObject getAsJsonObject(final JsonObject jsonObject1, final String string, final JsonObject jsonObject3)
    {
        if (jsonObject1.has(string))
        {
            return convertToJsonObject(jsonObject1.get(string), string);
        }

        return jsonObject3;
    }

    public static JsonArray convertToJsonArray(final JsonElement jsonElement, final String string)
    {
        if (jsonElement.isJsonArray())
        {
            return jsonElement.getAsJsonArray();
        }

        throw new JsonSyntaxException("Expected " + string + " to be a JsonArray, was " + getType(jsonElement));
    }

    public static JsonArray getAsJsonArray(final JsonObject jsonObject, final String string)
    {
        if (jsonObject.has(string))
        {
            return convertToJsonArray(jsonObject.get(string), string);
        }

        throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonArray");
    }

    @Nullable
    public static JsonArray getAsJsonArray(final JsonObject jsonObject, final String string, @Nullable final JsonArray jsonArray)
    {
        if (jsonObject.has(string))
        {
            return convertToJsonArray(jsonObject.get(string), string);
        }

        return jsonArray;
    }

    public static <T> T convertToObject(@Nullable final JsonElement jsonElement, final String string, final JsonDeserializationContext jsonDeserializationContext, final Class<? extends T> class4)
    {
        if (jsonElement != null)
        {
            return jsonDeserializationContext.<T>deserialize(jsonElement, class4);
        }

        throw new JsonSyntaxException("Missing " + string);
    }

    public static <T> T getAsObject(final JsonObject jsonObject, final String string, final JsonDeserializationContext jsonDeserializationContext, final Class<? extends T> class4)
    {
        if (jsonObject.has(string))
        {
            return GsonHelper.<T>convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class4);
        }

        throw new JsonSyntaxException("Missing " + string);
    }

    public static <T> T getAsObject(final JsonObject jsonObject, final String string, final T object, final JsonDeserializationContext jsonDeserializationContext, final Class<? extends T> class5)
    {
        if (jsonObject.has(string))
        {
            return GsonHelper.<T>convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class5);
        }

        return object;
    }

    public static String getType(final JsonElement jsonElement)
    {
        final String string2 = StringUtils.abbreviateMiddle(String.valueOf(jsonElement), "...", 10);

        if (jsonElement == null)
        {
            return "null (missing)";
        }

        if (jsonElement.isJsonNull())
        {
            return "null (json)";
        }

        if (jsonElement.isJsonArray())
        {
            return "an array (" + string2 + ")";
        }

        if (jsonElement.isJsonObject())
        {
            return "an object (" + string2 + ")";
        }

        if (jsonElement.isJsonPrimitive())
        {
            final JsonPrimitive jsonPrimitive3 = jsonElement.getAsJsonPrimitive();

            if (jsonPrimitive3.isNumber())
            {
                return "a number (" + string2 + ")";
            }

            if (jsonPrimitive3.isBoolean())
            {
                return "a boolean (" + string2 + ")";
            }
        }

        return string2;
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final Reader reader, final Class<T> class3, final boolean boolean4)
    {
        try
        {
            final JsonReader jsonReader5 = new JsonReader(reader);
            jsonReader5.setLenient(boolean4);
            return gson.<T>getAdapter(class3).read(jsonReader5);
        }
        catch (IOException iOException5)
        {
            throw new JsonParseException(iOException5);
        }
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final Reader reader, final Type type, final boolean boolean4)
    {
        try
        {
            final JsonReader jsonReader5 = new JsonReader(reader);
            jsonReader5.setLenient(boolean4);
            return (T)gson.getAdapter(TypeToken.get(type)).read(jsonReader5);
        }
        catch (IOException iOException5)
        {
            throw new JsonParseException(iOException5);
        }
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final String string, final Type typeToken, final boolean boolean4)
    {
        return GsonHelper.<T>fromJson(gson, new StringReader(string), typeToken, boolean4);
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final String string, final Class<T> class3, final boolean boolean4)
    {
        return GsonHelper.<T>fromJson(gson, new StringReader(string), class3, boolean4);
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final Reader reader, final Type typeToken)
    {
        return GsonHelper.<T>fromJson(gson, reader, typeToken, false);
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final String string, final Type typeToken)
    {
        return GsonHelper.<T>fromJson(gson, string, typeToken, false);
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final Reader reader, final Class<T> class3)
    {
        return GsonHelper.<T>fromJson(gson, reader, class3, false);
    }

    @Nullable
    public static <T> T fromJson(final Gson gson, final String string, final Class<T> class3)
    {
        return GsonHelper.<T>fromJson(gson, string, class3, false);
    }

    public static JsonObject parse(final String string, final boolean boolean2)
    {
        return parse(new StringReader(string), boolean2);
    }

    public static JsonObject parse(final Reader reader, final boolean boolean2)
    {
        return GsonHelper.<JsonObject>fromJson(GsonHelper.GSON, reader, JsonObject.class, boolean2);
    }

    public static JsonObject parse(final String string)
    {
        return parse(string, false);
    }

    public static JsonObject parse(final Reader reader)
    {
        return parse(reader, false);
    }

    static
    {
        GSON = new GsonBuilder().create();
    }
}
