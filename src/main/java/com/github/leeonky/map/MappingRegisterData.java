package com.github.leeonky.map;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class MappingRegisterData {
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> sourceViewScopeMappingMap = new HashMap<>();
    private Map<Class<?>, Map<Class<?>, List<Class<?>>>> viewScopeMappingListMap = new HashMap<>();

    void register(Class<?> mapFrom, Class<?> view, Class<?> scope, Class<?> mapTo) {
        sourceViewScopeMappingMap.computeIfAbsent(mapFrom, f -> new HashMap<>()).computeIfAbsent(view, f -> new HashMap<>()).put(scope, mapTo);
        viewScopeMappingListMap.computeIfAbsent(view, f1 -> new HashMap<>()).computeIfAbsent(scope, f2 -> new ArrayList<>()).add(mapTo);
    }

    Optional<Class<?>> findMapTo(Object mapFrom, Class<?> view, Class<?> scope) {
        Map<Class<?>, Class<?>> scopeMapping = sourceViewScopeMappingMap.getOrDefault(mapFrom.getClass(), emptyMap())
                .getOrDefault(view, emptyMap());
        Class<?> to = scopeMapping.get(scope);
        return Optional.ofNullable(to != null ? to : scopeMapping.get(void.class));
    }

    List<Class<?>> findAllSubMapTo(Class<?> baseMapping, Class<?> view, Class<?> scope) {
        Map<Class<?>, List<Class<?>>> scopeDestListMap = viewScopeMappingListMap.getOrDefault(view, emptyMap());
        return Stream.concat(scopeDestListMap.getOrDefault(scope, emptyList()).stream(),
                scopeDestListMap.getOrDefault(void.class, emptyList()).stream())
                .filter(baseMapping::isAssignableFrom)
                .collect(Collectors.toList());
    }
}