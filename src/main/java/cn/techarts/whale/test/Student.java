package cn.techarts.whale.test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;
//import jakarta.inject.Qualifier;

@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Student {

}
