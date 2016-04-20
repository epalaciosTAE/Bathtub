package com.tae.bathtub.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.security.RunAs;
import javax.inject.Scope;

/**
 * Created by Eduardo on 18/04/2016.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScope {
}
