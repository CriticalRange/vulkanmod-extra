package com.criticalrange.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapping helper for version-specific code differences
 * Handles mapping differences between Minecraft versions through reflection
 * and cached lookups for performance optimization
 * 
 * This class helps resolve version-specific method names, field names, and
 * class relocations that occur between Minecraft versions.
 */
public class MappingHelper {
    
    // Cache for resolved mappings to improve performance
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    // Version-specific mappings
    private static final Map<String, String> METHOD_ALIASES = new HashMap<>();
    private static final Map<String, String> FIELD_ALIASES = new HashMap<>();
    private static final Map<String, String> CLASS_ALIASES = new HashMap<>();
    
    static {
        // Initialize version-specific mappings
        initMappings();
    }
    
    private static void initMappings() {
        // Example mappings - add actual version-specific differences here
        
        // 1.21.1 vs 1.21.2 differences
        if (VersionHelper.IS_1_21_1) {
            // 1.21.1 specific mappings
            METHOD_ALIASES.put("renderWorld", "renderLevel");
            METHOD_ALIASES.put("setupCamera", "prepareCamera");
        } else {
            // 1.21.2+ specific mappings  
            METHOD_ALIASES.put("renderLevel", "renderWorld");
            METHOD_ALIASES.put("prepareCamera", "setupCamera");
        }
        
        // 1.21.3+ changes
        if (VersionHelper.IS_POST_1_21_2) {
            METHOD_ALIASES.put("loadChunk", "loadChunkAsync");
            FIELD_ALIASES.put("level", "world");
        }
        
        // 1.21.4+ changes
        if (VersionHelper.IS_POST_1_21_3) {
            METHOD_ALIASES.put("tickWorld", "tickWorld");
            CLASS_ALIASES.put("net.minecraft.world.level.Level", "net.minecraft.server.level.WorldServer");
        }
    }
    
    /**
     * Get a method with version-aware fallback support
     * @param targetClass Class containing the method
     * @param methodName Primary method name
     * @param parameterTypes Method parameter types
     * @return Resolved method or null if not found
     */
    public static Method getMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
        try {
            // First try to get the method using the primary name
            String cacheKey = targetClass.getName() + "#" + methodName + getParameterSignature(parameterTypes);
            
            // Check cache first
            Method cachedMethod = methodCache.get(cacheKey);
            if (cachedMethod != null) {
                return cachedMethod;
            }
            
            // Try primary method name
            try {
                Method method = targetClass.getMethod(methodName, parameterTypes);
                methodCache.put(cacheKey, method);
                return method;
            } catch (NoSuchMethodException e1) {
                // Try method aliases
                for (Map.Entry<String, String> entry : METHOD_ALIASES.entrySet()) {
                    if (entry.getValue().equals(methodName)) {
                        try {
                            Method method = targetClass.getMethod(entry.getKey(), parameterTypes);
                            methodCache.put(cacheKey, method);
                            return method;
                        } catch (NoSuchMethodException e2) {
                            // Continue to next alias
                        }
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            // Fallback to reflection without caching
            return findMethod(targetClass, methodName, parameterTypes);
        }
    }
    
    /**
     * Get a field with version-aware fallback support
     * @param targetClass Class containing the field
     * @param fieldName Primary field name
     * @return Resolved field or null if not found
     */
    public static Field getField(Class<?> targetClass, String fieldName) {
        try {
            // Check cache first
            String cacheKey = targetClass.getName() + "#" + fieldName;
            Field cachedField = fieldCache.get(cacheKey);
            if (cachedField != null) {
                return cachedField;
            }
            
            // Try primary field name
            try {
                Field field = targetClass.getField(fieldName);
                fieldCache.put(cacheKey, field);
                return field;
            } catch (NoSuchFieldException e1) {
                // Try field aliases
                for (Map.Entry<String, String> entry : FIELD_ALIASES.entrySet()) {
                    if (entry.getValue().equals(fieldName)) {
                        try {
                            Field field = targetClass.getField(entry.getKey());
                            fieldCache.put(cacheKey, field);
                            return field;
                        } catch (NoSuchFieldException e2) {
                            // Continue to next alias
                        }
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            // Fallback to reflection without caching
            return findField(targetClass, fieldName);
        }
    }
    
    /**
     * Get a class with version-aware resolution
     * @param className Class name to resolve
     * @return Resolved class or null if not found
     */
    public static Class<?> getClass(String className) {
        try {
            // Check cache first
            Class<?> cachedClass = classCache.get(className);
            if (cachedClass != null) {
                return cachedClass;
            }
            
            // Try primary class name
            try {
                Class<?> clazz = Class.forName(className);
                classCache.put(className, clazz);
                return clazz;
            } catch (ClassNotFoundException e1) {
                // Try class aliases
                for (Map.Entry<String, String> entry : CLASS_ALIASES.entrySet()) {
                    if (entry.getValue().equals(className)) {
                        try {
                            Class<?> clazz = Class.forName(entry.getKey());
                            classCache.put(className, clazz);
                            return clazz;
                        } catch (ClassNotFoundException e2) {
                            // Continue to next alias
                        }
                    }
                }
            }
            
            return null;
            
        } catch (Exception ex) {
            // Fallback to class loading without caching
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
    
    /**
     * Invoke a method with version-aware resolution
     * @param target Object to invoke method on (null for static)
     * @param methodName Method name to invoke
     * @param parameterTypes Method parameter types
     * @param parameters Method parameters
     * @return Method result or null if invocation fails
     */
    public static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Method method = getMethod(target.getClass(), methodName, parameterTypes);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(target, parameters);
            }
            return null;
        } catch (Exception e) {
            // Log for debugging but don't crash
            System.err.println("[VulkanMod Extra] Method invocation failed: " + methodName + " on " + target.getClass().getName());
            return null;
        }
    }
    
    /**
     * Get a field value with version-aware resolution
     * @param target Object to get field from (null for static)
     * @param fieldName Field name to get
     * @return Field value or null if access fails
     */
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = getField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
            return null;
        } catch (Exception e) {
            // Log for debugging but don't crash
            System.err.println("[VulkanMod Extra] Field access failed: " + fieldName + " on " + target.getClass().getName());
            return null;
        }
    }
    
    /**
     * Set a field value with version-aware resolution
     * @param target Object to set field on (null for static)
     * @param fieldName Field name to set
     * @param value Value to set
     * @return true if set successfully, false otherwise
     */
    public static boolean setFieldValue(Object target, String fieldName, Object value) {
        try {
            Field field = getField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
                return true;
            }
            return false;
        } catch (Exception e) {
            // Log for debugging but don't crash
            System.err.println("[VulkanMod Extra] Field set failed: " + fieldName + " on " + target.getClass().getName());
            return false;
        }
    }
    
    // Helper methods
    private static Method findMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
        try {
            return targetClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e1) {
            // Try with different parameter combinations
            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName) && 
                    isCompatibleParameterTypes(method.getParameterTypes(), parameterTypes)) {
                    return method;
                }
            }
            return null;
        }
    }
    
    private static Field findField(Class<?> targetClass, String fieldName) {
        try {
            return targetClass.getField(fieldName);
        } catch (NoSuchFieldException e1) {
            // Try with all fields
            for (Field field : targetClass.getFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            return null;
        }
    }
    
    private static boolean isCompatibleParameterTypes(Class<?>[] methodParams, Class<?>[] givenParams) {
        if (methodParams.length != givenParams.length) {
            return false;
        }
        
        for (int i = 0; i < methodParams.length; i++) {
            if (!methodParams[i].isAssignableFrom(givenParams[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    private static String getParameterSignature(Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> paramType : parameterTypes) {
            sb.append(paramType.getName()).append(",");
        }
        return sb.toString();
    }
    
    /**
     * Clear the mapping cache (useful for development/reloading)
     */
    public static void clearCache() {
        methodCache.clear();
        fieldCache.clear();
        classCache.clear();
    }
    
    /**
     * Get cache statistics for debugging
     * @return String with cache statistics
     */
    public static String getCacheStats() {
        return String.format("Method Cache: %d entries, Field Cache: %d entries, Class Cache: %d entries",
            methodCache.size(), fieldCache.size(), classCache.size());
    }
}