package me.cxdev.commerce.toolkit.testing.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
	public static <T extends Exception> void throwException(Class<T> exceptionClass) throws T {
		T exceptionToThrow = getExceptionWithoutArguments(exceptionClass);
		if (exceptionToThrow == null) {
			exceptionToThrow = getExceptionWithArgument(exceptionClass, "Forced to throw: " + exceptionClass.getSimpleName());
		}
		if (exceptionToThrow == null) {
			exceptionToThrow = getExceptionWithArgument(exceptionClass, new Throwable("Forced to throw: " + exceptionClass.getSimpleName()));
		}

		if (exceptionToThrow != null) {
			throw exceptionToThrow;
		} else {
			throw new IllegalStateException("Cannot instantiate desired exception for class: " + exceptionClass.getSimpleName());
		}
	}

	private static <T extends Exception> T getExceptionWithoutArguments(Class<T> exceptionClass) {
		try {
			Constructor<T> constructor = exceptionClass.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	private static <T extends Exception> T getExceptionWithArgument(Class<T> exceptionClass, Object argument) {
		try {
			Constructor<T> constructor = exceptionClass.getConstructor(argument.getClass());
			return constructor.newInstance(argument);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	public static boolean isVoidMethod(Method method) {
		return isVoid(method.getReturnType());
	}

	public static boolean isVoid(Class<?> type) {
		return type.getName() == "void" || type == Void.class;
	}
}
