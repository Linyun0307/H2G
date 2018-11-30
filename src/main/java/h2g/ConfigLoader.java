package h2g;

import java.io.*;
import javax.json.*;

public class ConfigLoader {
    public JsonObject obj;
    boolean hasLoaded = false;

    public Object get(String path) throws Exception {
        if (!hasLoaded) throw new Exception("Json file hasn't been loaded!");
        String[] tokens = path.split("\\.");
        Object o = (Object) obj;
        for (String token : tokens) {
            if (o instanceof JsonArray) {
                int index = Integer.valueOf(token);
                o = (Object) (((JsonArray) o).get(index));
            } else if (o instanceof JsonObject) {
                o = (Object) (((JsonObject) o).get((Object) token));
            } else {
                throw new Exception("Path is not valid!");
            }
        }
        return o;
    }

    public String getStr(String path) throws Exception {
        Object o = get(path);
        if (o instanceof JsonString) return ((JsonString) o).getString();
        else {
            new Exception("Failed to cast!");
            return null;
        }
    }

    public int getInt(String path) throws Exception {
        Object o = get(path);
        if (o instanceof JsonNumber) return ((JsonNumber) o).intValue();
        else {
            new Exception("Failed to cast!");
            return 0;
        }
    }

    public double getDouble(String path) throws Exception {
        Object o = get(path);
        if (o instanceof JsonNumber) return ((JsonNumber) o).doubleValue();
        else {
            new Exception("Failed to cast!");
            return 0;
        }
    }

    public boolean getBool(String path) throws Exception {
        Object o = get(path);
        if (o instanceof JsonValue) {
            switch (((JsonValue) o).getValueType()) {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                default:
                    new Exception("Failed to cast!");
                    return false;
            }
        } else {
            new Exception("Failed to cast!");
            return false;
        }
    }

    public ConfigLoader(String fileName) throws IOException {
        InputStream is = new FileInputStream(new File(fileName));
        JsonReader rdr = Json.createReader(is);
        obj = rdr.readObject();
        hasLoaded = true;
            /*
            JsonArray results = obj.getJsonArray("data");
            for (JsonObject result : results.getValuesAs(JsonObject.class)) {
                System.out.print(result.getJsonObject("from").getString("name"));
                System.out.print(": ");
                System.out.println(result.getString("message", ""));
                System.out.println("-----------");
            }*/
    }

    public static void main(String[] args) {
        try {
            ConfigLoader cL = new ConfigLoader("facebook.json");
            String str = cL.getStr("data.1.message");
            int n = cL.getInt("data.0.int");
            boolean b = cL.getBool("data.0.bool");
            double d = cL.getDouble("data.0.double");
            System.out.println(d);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
