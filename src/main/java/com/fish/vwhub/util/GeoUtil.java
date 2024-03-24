package com.fish.vwhub.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeoUtil {

    private static final Map<String, Float[]> GEO_CACHE = new ConcurrentHashMap<>();

    public static void saveGeo(String name, Float[] value) {
        GEO_CACHE.put(name, value);
    }

    public static Float[] getGeo(String name) {
        return GEO_CACHE.get(name);
    }

}
