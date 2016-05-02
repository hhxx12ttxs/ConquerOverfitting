/*
 This file is part of Squash.

 Squash is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, 
 or any later version.

 Squash is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with Squash.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.omik.squash;

import com.google.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omik.squash.provider.ISquashProvider;
import se.omik.squash.provider.NoCacheProvider;
import se.omik.squash.utils.SquashSettings;

/**
 * @author Jeremy Comte
 */
@Aspect
public class SquashAdviser {

    private Logger logger = LoggerFactory.getLogger(SquashAdviser.class);

    @Inject
    private ISquashProvider squashProvider;

    public SquashAdviser() {

    }

    public void destroy() {
        logger.info("Destroying Squash adviser");
    }

    private void init() {
        logger.info("Initialising Squash adviser");
        if (SquashSettings.getSettings().isEmpty()) {
            logger.warn("Settings are empty.");
        }

        String providerClassName = SquashSettings.getSettings().getProperty("squash.provider");
        Object provider = null;
        if (providerClassName != null) {
            try {
                Class providerClass = this.getClass().getClassLoader().loadClass(providerClassName);
                provider = providerClass.newInstance();
            } catch (InstantiationException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (IllegalAccessException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (ClassNotFoundException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        if (provider != null) {
            logger.info("CacheProvider is " + providerClassName);
            squashProvider = (ISquashProvider) provider;
        } else {
            logger.warn("Your caching is not properly configured. See documentation.");
            logger.info("Settings are incorrect, using dummy NoCacheProvider.");
            logger.info("NoCacheProvider doesn't provide any caching, just passthrough without any memory usage.");
            squashProvider = new NoCacheProvider();
        }
    }

    //Aspect methods from here
    private String getKey(String domain, String path, Object... methodArgs) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        if (squashProvider == null) {
            init();
        }
        return getKey(null, domain, path, methodArgs);
    }

    private String getKey(Object object, String domain, String path, Object... methodArgs) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        String subKey = "";
        for (String element : path.split(":")) {
            Object subId = null;
            if (element.contains("()") && object != null) {
                element = element.replaceAll("\\(\\)", "");
                subId = object.getClass().getMethod(element.trim()).invoke(object);
            } else if (element.matches("\\$.*")) {
                element = element.replaceAll("\\$", "");
                if (element.matches("[0-9]+")) {
                    subId = methodArgs[Integer.parseInt(element)];
                } else if (object != null) {
                    for (Field field : object.getClass().getFields()) {
                        if (field.getName().equals(element)) {
                            subId = field.get(object).toString();
                            break;
                        }
                    }
                }
            } else {
                subId = element.trim();
            }
            if (subId != null && subId.toString().length() > 0) {
                subKey += (subKey.length() == 0) ? subId.toString() : ":" + subId.toString();
            } else {
                if (object != null) {
                    logger.warn("element " + element + " couldn't be found on " + object.getClass().getSimpleName());
                }
                subKey = "";
                break;
            }
        }
        if (subKey.length() > 0 && domain != null && !subKey.contains(domain)) {
            subKey = domain + ":" + subKey;
        }

        return subKey.toLowerCase();
    }

    private void updateKeys(Object object, String domain, String keys, int expiration, boolean useKeyList, int mainKey, Object... methodArgs) {
        updateKeys(object, domain, keys, expiration, useKeyList, mainKey, false, methodArgs);
    }

    private void updateKeys(Object object, String domain, String keys, int expiration, boolean useKeyList, int mainKey, boolean newOnly, Object... methodArgs) {
        if (keys != null && keys.length() > 0) {
            try {
                if (logger.isDebugEnabled()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(object);
                    logger.debug("estimated serialized object size: " + baos.size() + " bytes");
                    oos.close();
                }
                String[] splittedKeys = keys.split(",");
                String keylistKey = null;
                ArrayList<String> updatedKeys = new ArrayList<String>();
                ArrayList<String> previousKeys = null;

                for (int i = 0; i < splittedKeys.length; i++) {
                    String key = getKey(object, domain, splittedKeys[i], methodArgs);
                    if (key.length() > 0) {
                        if (useKeyList) {
                            if (splittedKeys.length > 1 && (i == mainKey || (mainKey == -1 && keylistKey == null /*&& (!keys.contains("$id") || splittedKeys[i].contains("$id"))*/))) {
                                keylistKey = key + "->keylist";
                                previousKeys = (ArrayList<String>) squashProvider.get(keylistKey);
                            }
                            updatedKeys.add(key);
                        }
                        squashProvider.set(key, object, expiration, newOnly);
                    }
                }

                if (updatedKeys.size() > 1) {
                    if (previousKeys != null) {
                        for (String updatedKey : updatedKeys) {
                            for (int i = 0; i < previousKeys.size(); i++) {
                                if (updatedKey.equals(previousKeys.get(i))) {
                                    previousKeys.remove(i);
                                    i++;
                                }
                            }
                        }
                        if (previousKeys.size() > 0) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("cleaning up keys...");
                            }
                            for (String previousKey : previousKeys) {
                                squashProvider.delete(previousKey);
                            }
                        }
                    }
                    squashProvider.set(keylistKey, updatedKeys);
                }

            } catch (Exception e) {
                logger.error("check the updateEntries parameter on cacheUpdate", e);
            }
        }
    }

    private void deleteKeys(Object object, String domain, String keys, boolean useKeyList, Object... methodArgs) {
        if (keys != null && keys.length() > 0) {
            try {
                for (String path : keys.split(",")) {
                    String key = getKey(object, domain, path, methodArgs);
                    if (key.length() > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("deleting key: " + key);
                        }
                        squashProvider.delete(key);
                    }
                    if (useKeyList) {
                        String keylistKey = key + "->keylist";
                        ArrayList<String> keyList = (ArrayList<String>) squashProvider.get(keylistKey);
                        if (keyList != null) {
                            for (String subKey : keyList) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("deleting subkey: " + subKey);
                                }
                                squashProvider.delete(subKey);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("check the updateEntries parameter on cacheDelete", e);
            }
        }
    }

    private String getDomain(JoinPoint joinPoint) {
        SquashDomain cacheDomain;
        if (joinPoint.getTarget() != null) {
            cacheDomain = joinPoint.getTarget().getClass().getAnnotation(SquashDomain.class);
        } else {
            cacheDomain = ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(SquashDomain.class);
        }
        if (cacheDomain != null) {
            return cacheDomain.value();
        }
        return null;
    }

    /**
     * Aspect applied to read operations.
     */
    @Around(value = "execution(* *(..)) && @annotation(squashBefore)")
    public Object readMemcached(ProceedingJoinPoint joinPoint, SquashBefore squashBefore) throws Throwable {
        long start = System.currentTimeMillis();
        String domain = getDomain(joinPoint);
        String keyToRead = squashBefore.key();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (keyToRead == null || keyToRead.length() == 0) {
            StringBuilder guessedKey = new StringBuilder();
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                guessedKey.append(":$").append(i);
            }
            if (guessedKey.length() == 0) {
                guessedKey.append(":").append(methodSignature.getName());
            }
            if (guessedKey.length() > 0) {
                keyToRead = methodSignature.getReturnType().getSimpleName() + guessedKey.toString();
            }

        }
        String key = getKey(domain, keyToRead, joinPoint.getArgs());//get the key of the object
        int expiration = squashBefore.expiration();
        if (expiration == -1 && squashBefore.expirationFromParameter() >= 0) {
            expiration = Integer.parseInt(String.valueOf(joinPoint.getArgs()[squashBefore.expirationFromParameter()]));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("cacheRead: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName() + " [" + key + "]");
        }
        Object object = squashProvider.get(key);//try to get the object from the cache
        if (object == null) {//if the object is not in the cache...
            object = joinPoint.proceed();//invoke the method 
            if (logger.isDebugEnabled()) {
                logger.debug("object didn't exist in cache, executing method: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName() + " took " + (System.currentTimeMillis() - start) + "ms");
            }
            if (squashBefore.cacheWhenNull() && object == null) {
                object = "null";
            }
            if (object != null) {
                String keys = key;
                if (squashBefore.updateOtherEntries().length() > 0) {
                    keys += "," + squashBefore.updateOtherEntries();
                }
                updateKeys(object, domain, keys, expiration, squashBefore.useKeyList(), squashBefore.mainKey(), joinPoint.getArgs());
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("returning cached object");
        }
        if ("null".equals(object)) {
            object = null;
        }
        return object;
    }

    /**
     * Aspect applied to delete operations.
     */
    @Around(value = "execution(* *(..)) && @annotation(deleteFromSquash)")
    public void deleteMemcached(ProceedingJoinPoint joinPoint, DeleteFromSquash deleteFromSquash) throws Throwable {
        joinPoint.proceed();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (logger.isDebugEnabled()) {
            logger.debug("cacheDelete: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName() + " [" + joinPoint.getArgs()[0] + "]");
        }
        String domain = getDomain(joinPoint);
        String deleteEntries = deleteFromSquash.deleteEntries();
        if (deleteEntries == null || deleteEntries.length() == 0) {
            deleteEntries = ((MethodSignature) joinPoint.getSignature()).getReturnType().getSimpleName() + ":$" + deleteFromSquash.keyField();
            if (logger.isDebugEnabled()) {
                logger.debug("guessed: " + deleteEntries);
            }
        }
        deleteKeys(joinPoint.getArgs()[0], domain, deleteEntries, deleteFromSquash.useKeyList(), joinPoint.getArgs());
    }

    /**
     * Aspect applied to update operations.
     */
    @AfterReturning(value = "execution(* *(..)) && @annotation(squashAfter)", returning = "object")
    public void updateMemcached(JoinPoint joinPoint, Object object, SquashAfter squashAfter) {
        if (object != null) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            if (logger.isDebugEnabled()) {
                logger.debug("cacheUpdate: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName() + " [" + object + "]");
            }
            String domain = getDomain(joinPoint);
            String updateEntries = squashAfter.updateEntries();
            int expiration = squashAfter.expiration();
            if (expiration == -1 && squashAfter.expirationFromParameter() >= 0) {
                expiration = Integer.parseInt(String.valueOf(joinPoint.getArgs()[squashAfter.expirationFromParameter()]));
            }
            if (updateEntries == null || updateEntries.length() == 0) {
                updateEntries = methodSignature.getReturnType().getSimpleName() + ":$" + squashAfter.keyField();
                if (logger.isDebugEnabled()) {
                    logger.debug("guessed: " + updateEntries);
                }
            }
            updateKeys(object, domain, updateEntries, expiration, squashAfter.useKeyList(), squashAfter.mainKey(), joinPoint.getArgs());
            deleteKeys(object, domain, squashAfter.deleteEntries(), false, joinPoint.getArgs());
        }
    }

    /**
     * Aspect applied to collection operations.
     */
    @AfterReturning(value = "execution(* *(..)) && @annotation(squashedCollection)", returning = "collection")
    public void cacheListElementsInMemcached(JoinPoint joinPoint, Object collection, SquashedCollection squashedCollection) {
        if (collection != null) {
            if (collection instanceof Collection) {
                for (Object object : (Collection) collection) {
                    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                    if (logger.isDebugEnabled()) {
                        logger.debug("cacheUpdate: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName() + " [" + object + "]");
                    }
                    String domain = getDomain(joinPoint);
                    String updateEntries = squashedCollection.updateEntries();
                    int expiration = squashedCollection.expiration();
                    if (expiration == -1 && squashedCollection.expirationFromParameter() >= 0) {
                        expiration = Integer.parseInt(String.valueOf(joinPoint.getArgs()[squashedCollection.expirationFromParameter()]));
                    }
                    if (updateEntries == null || updateEntries.length() == 0) {
                        updateEntries = methodSignature.getReturnType().getSimpleName() + ":$" + squashedCollection.keyField();
                        if (logger.isDebugEnabled()) {
                            logger.debug("guessed: " + updateEntries);
                        }
                    }
                    updateKeys(object, domain, updateEntries, expiration, false, 0, squashedCollection.newOnly(), joinPoint.getArgs());
                }
            } else {
                logger.error("squashedCollection annotation around a method that doesn't return a collection ...");
            }
        }
    }

    @AfterReturning(value = "execution(* *(..)) && @annotation(flushAllFromSquash)", returning = "object")
    public void flushMemcached(JoinPoint joinPoint, Object object, FlushAllFromSquash flushAllFromSquash) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (logger.isDebugEnabled()) {
            logger.debug("cacheFlush: " + methodSignature.getMethod().getDeclaringClass().getSimpleName() + "." + methodSignature.getMethod().getName());
        }
        squashProvider.flushAll();
    }

    @Around(value = "execution(* *(..)) && @annotation(squashInfo)")
    public String getInfo(ProceedingJoinPoint joinPoint, SquashInfo squashInfo) {
        return squashProvider.getInfo();
    }
}

