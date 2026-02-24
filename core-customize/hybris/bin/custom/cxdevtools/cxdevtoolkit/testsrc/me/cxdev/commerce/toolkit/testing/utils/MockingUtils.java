package me.cxdev.commerce.toolkit.testing.utils;

import static me.cxdev.commerce.toolkit.testing.utils.ReflectionUtils.isVoid;
import static me.cxdev.commerce.toolkit.testing.utils.ReflectionUtils.isVoidMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.EnumUtils;

public class MockingUtils {
	private static final String ZERO_VALUE = "0";

	public static <T, I> void injectDelegateMock(T instanceUnderTest, String setterName, Class<I> delegateInterface, I delegate) {
		try {
			Method injectorMethod = instanceUnderTest.getClass().getDeclaredMethod(setterName, delegateInterface);
			injectorMethod.invoke(instanceUnderTest, delegate);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException("Could not inject delegate with setter '" + setterName + "' on instance!", e);
		}
	}

	public static <I> void prepareMethodInvocationOnMock(Method method, I delegate, Object[] mockedParameters, Object mockedReturnValue) {
		if (!isVoidMethod(method)) {
			try {
				when(method.invoke(delegate, mockedParameters)).thenReturn(mockedReturnValue);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("Could not prepare mock for invocation of method '" + method.getName() + "' on delegate mock!", e);
			}
		}
	}

	public static <T> Object invokeWithParameterStubs(T instanceUnderTest, Method method, Object[] mockedParameters) {
		try {
			if (isVoidMethod(method)) {
				method.invoke(instanceUnderTest, mockedParameters);
				return null;
			} else {
				return method.invoke(instanceUnderTest, mockedParameters);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Could not invoke method '" + method.getName() + "' on instance!", e);
		}
	}

	public static Object[] getMocksForParameters(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			parameters[i] = getMockForClass(parameterType);
		}
		return parameters;
	}

	public static <T> T getMockForClass(Class<T> parameterType) {
		if (isVoid(parameterType)) {
			return null;
		} else if (parameterType.isPrimitive()) {
			return getMockForPrimitive(parameterType);
		} else if (ClassUtils.isPrimitiveWrapper(parameterType)) {
			Class<?> primitiveType = ClassUtils.wrapperToPrimitive(parameterType);
			return (T) getMockForPrimitive(primitiveType);
		} else if (parameterType.isArray()) {
			return (T) getMockForArray(parameterType.getComponentType());
		} else if (parameterType.isEnum()) {
			return (T) EnumUtils.getEnumList((Class<? extends Enum>) parameterType).get(0);
		} else if (parameterType == String.class) {
			return (T) "mock";
		} else {
			T mock = getMockForCollectionFrameworkInterfaces(parameterType);
			return mock != null ? mock : mock(parameterType);
		}
	}

	private static <T> T getMockForCollectionFrameworkInterfaces(Class<T> parameterType) {
		if (!parameterType.isInterface()) {
			return null;
		}

		if (Map.class.isAssignableFrom(parameterType)) {
			return (T) new TreeMap<>();
		} else if (Set.class.isAssignableFrom(parameterType)) {
			return (T) new TreeSet<>();
		} else if (Collection.class.isAssignableFrom(parameterType)) {
			return (T) new LinkedList<>();
		} else {
			return null;
		}
	}

	private static <T> T getMockForPrimitive(Class<T> parameterType) {
		if (parameterType.getName() == "char") {
			return (T) Character.valueOf(ZERO_VALUE.charAt(0));
		} else if (parameterType.getName() == "boolean") {
			return (T) Boolean.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "byte") {
			return (T) Byte.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "short") {
			return (T) Short.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "int") {
			return (T) Integer.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "long") {
			return (T) Long.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "double") {
			return (T) Double.valueOf(ZERO_VALUE);
		} else if (parameterType.getName() == "float") {
			return (T) Float.valueOf(ZERO_VALUE);
		} else {
			return null;
		}
	}

	private static <T> T[] getMockForArray(Class<T> componentType) {
		T[] arrayWithMocks = (T[]) Array.newInstance(componentType, 2);
		arrayWithMocks[0] = getMockForClass(componentType);
		arrayWithMocks[1] = getMockForClass(componentType);
		return arrayWithMocks;
	}
}
