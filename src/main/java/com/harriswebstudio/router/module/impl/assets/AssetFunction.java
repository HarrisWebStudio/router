package com.harriswebstudio.router.module.impl.assets;

@FunctionalInterface
public interface AssetFunction<T, R> {

    R apply(T t) throws Exception;

}