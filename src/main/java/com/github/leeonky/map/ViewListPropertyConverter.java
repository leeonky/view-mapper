package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewListPropertyConverter extends ViewConverter {
    protected final String propertyName;

    public ViewListPropertyConverter(Mapper mapper, Class<?> view, String propertyName) {
        super(mapper, view);
        this.propertyName = propertyName;
    }

    @SuppressWarnings("unchecked")
    public static Iterable wrapperEntry(Map map) {
        return (Iterable) map.entrySet().stream()
                .map(e -> new Entry((Map.Entry) e))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static Object getPropertyValue(Object e, String propertyChain) {
        for (String property : propertyChain.split("\\."))
            e = ((BeanClass) BeanClass.create(e.getClass())).getPropertyValue(e, property);
        return e;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        if (Iterable.class.isAssignableFrom(rawType))
            return mapCollection(collection, newCollection(rawType), mappingContext);
        else if (rawType.isArray())
            return mapCollection(collection, new ArrayList<>(), mappingContext).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        throw new IllegalStateException("Only support map " + propertyName + " to list or array, but target type is " + rawType.getName());
    }

    private Collection<Object> mapCollection(Iterable source, Collection<Object> result, MappingContext mappingContext) {
        for (Object e : source)
            result.add(map(getPropertyValue(e, propertyName), mappingContext));
        return result;
    }

    @Override
    public String buildConvertId() {
        return String.format("ViewListPropertyConverter:%s:%s[%d]", propertyName, view.getName(), mapper.hashCode());
    }

    public static class Entry {
        private Object key, value;

        public Entry(Map.Entry e) {
            key = e.getKey();
            value = e.getValue();
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
