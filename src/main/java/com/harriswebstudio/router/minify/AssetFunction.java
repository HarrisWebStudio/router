package com.harriswebstudio.router.minify;

@FunctionalInterface
public interface AssetFunction<T, R> {

    R apply(T t) throws Exception;

}