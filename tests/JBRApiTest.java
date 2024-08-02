/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @test
 * @run main JBRApiTest
 */

import com.jetbrains.Extensions;
import com.jetbrains.JBR;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JBRApiTest {

    // These services may not always be supported and usually have their own dedicated tests.
    private static final Set<String> IGNORED_SERVICES = new HashSet<>();

    public static void main(String[] args) throws Exception {
        IGNORED_SERVICES.add("com.jetbrains.RoundedCornersManager");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            IGNORED_SERVICES.add("com.jetbrains.WindowDecorations");
            IGNORED_SERVICES.add("com.jetbrains.TextInput");
        } else if (os.contains("mac")) {
            IGNORED_SERVICES.add("com.jetbrains.WindowMove");
        } else {
            IGNORED_SERVICES.add("com.jetbrains.WindowMove");
            IGNORED_SERVICES.add("com.jetbrains.TextInput");
        }
        if (!JBR.getApiVersion().equals("SNAPSHOT") &&
            !JBR.getApiVersion().matches("\\d+\\.\\d+\\.\\d+")) throw new Error("Invalid API version: " + JBR.getApiVersion());
        if (!JBR.isAvailable()) throw new Error("JBR API is not available");
        List<String> knownServices = checkMetadata();
        testAllKnownServices(knownServices);
        testPublicServices();
        testExtensions();
    }

    private static List<String> checkMetadata() throws Exception {
        Class<?> metadata = Class.forName(JBR.class.getName() + "$Metadata");
        Field field = metadata.getDeclaredField("KNOWN_SERVICES");
        field.setAccessible(true);
        List<String> knownServices = List.of((String[]) field.get(null));
        if (!knownServices.contains("com.jetbrains.JBR$ServiceApi")) {
            throw new Error("com.jetbrains.JBR$ServiceApi was not found in known services of com.jetbrains.JBR$Metadata");
        }
        return knownServices;
    }

    private static void testAllKnownServices(List<String> knownServices) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ClassLoader classLoader = JBR.class.getClassLoader();
        Field serviceApiField = JBR.class.getDeclaredField("api");
        serviceApiField.setAccessible(true);
        Object serviceApi = serviceApiField.get(null);
        Method getServiceMethod = serviceApi.getClass().getDeclaredMethod("getService", Class.class);
        getServiceMethod.setAccessible(true);
        for (String serviceName : knownServices) {
            if (IGNORED_SERVICES.contains(serviceName)) continue;
            Class<?> serviceClass = Class.forName(serviceName, true, classLoader);
            Object service = getServiceMethod.invoke(serviceApi, serviceClass);
            if (service == null) throw new Error("Service " + serviceName + " is not supported");
        }
    }

    private static void testPublicServices() {
        Arrays.stream(JBR.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && m.getParameterCount() == 0 && m.getName().startsWith("get"))
                .forEach(m -> {
                    if (IGNORED_SERVICES.contains(m.getReturnType().getName())) return;
                    try {
                        Object service = m.invoke(null);
                        if (service == null) throw new Error(m.getName() + " returned null");
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void testExtensions() {
        if (System.getProperty("jetbrains.runtime.api.extensions.enabled", "true").equalsIgnoreCase("false")) return;
        for (Extensions ext : Extensions.values()) {
            if (!JBR.isExtensionSupported(ext)) {
                throw new Error("Extension " + ext.name() + " is not supported");
            }
        }
    }
}
