package org.rossonet.annotation;

public @interface ShellOption {

	String[] defaultValue() default {};

	String help();

}
