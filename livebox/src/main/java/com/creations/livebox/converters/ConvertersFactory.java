package com.creations.livebox.converters;

/**
 * @author Sérgio Serra on 26/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public interface ConvertersFactory<R> {
    <T> Converter<T, R> get(Class<T> aClass);
}
