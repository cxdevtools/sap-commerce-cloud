package me.cxdev.commerce.toolkit.testing.verifier;

import static me.cxdev.commerce.toolkit.testing.utils.MockingUtils.injectDelegateMock;
import static me.cxdev.commerce.toolkit.testing.utils.MockingUtils.invokeWithParameterStubs;
import static me.cxdev.commerce.toolkit.testing.utils.MockingUtils.prepareMethodInvocationOnMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.Invocation;

import me.cxdev.commerce.toolkit.testing.utils.MockingUtils;

public class DelegationPatternVerifier<T, I> {
	private T instanceUnderTest;
	private Class<I> delegatedInterface;
	private String setterName = "setDelegate";

	public static <T, I> DelegationPatternVerifier<T, I> verify(T instanceUnderTest, Class<I> delegatedInterface) {
		return new DelegationPatternVerifier<>(instanceUnderTest, delegatedInterface);
	}

	private DelegationPatternVerifier(T instanceUnderTest, Class<I> delegatedInterface) {
		this.instanceUnderTest = instanceUnderTest;
		this.delegatedInterface = delegatedInterface;
	}

	public DelegationPatternVerifier withSetter(String name) {
		this.setterName = name;
		return this;
	}

	public void all() {
		withoutMethodNames(Set.of());
	}

	public void withoutMethodNames(Set<String> excludedMethods) {
		Arrays.stream(delegatedInterface.getDeclaredMethods())
				.filter(method -> !method.isSynthetic())
				.filter(method -> !excludedMethods.contains(method.getName()))
				.forEach(method -> performVerificationForMethod(method));
	}

	private void performVerificationForMethod(Method method) {
		try {
			I delegate = mock(delegatedInterface);
			Object[] mockedParameters = MockingUtils.getMocksForParameters(method);
			Object mockedReturnValue = MockingUtils.getMockForClass(method.getReturnType());
			prepareMethodInvocationOnMock(method, delegate, mockedParameters, mockedReturnValue);
			injectDelegateMock(instanceUnderTest, setterName, delegatedInterface, delegate);

			Object result = invokeWithParameterStubs(instanceUnderTest, method, mockedParameters);
			verifyMethodInvocationOnDelegate(method, delegate, mockedParameters, mockedReturnValue, result);
		} catch (MockitoException e) {
			throw new AssertionError("Method " + method.getName() + " does cannot be verified due to Mockito exception!", e);
		}
	}

	private void verifyMethodInvocationOnDelegate(Method method, I delegate, Object[] mockedParameters, Object mockedReturnValue, Object result) {
		try {
			// Verify mock has only one invocation
			Collection<Invocation> invocations = mockingDetails(delegate).getInvocations();
			assertThat(invocations).hasSize(1);

			// Verify invoked method parameters match the mocked parameters
			Invocation invocation = invocations.stream().findFirst().get();
			assertThat(invocation.getRawArguments()).containsExactly(mockedParameters);

			// Verify invoked overloaded method conforms to interface method
			Method invokedMethod = invocation.getMethod();
			assertThat(invokedMethod.getReturnType()).isAssignableFrom(method.getReturnType());
			assertThat(invokedMethod.getName()).isEqualTo(method.getName());
			assertThat(invokedMethod.getParameterCount()).isEqualTo(method.getParameterCount());

			// Verify invoked overloaded method parameters types conform to interface parameter types
			Class<?>[] methodParameterTypes = method.getParameterTypes();
			Class<?>[] invokedMethodParameterTypes = invokedMethod.getParameterTypes();
			for (int i = 0; i < invokedMethodParameterTypes.length; i++) {
				assertThat(invokedMethodParameterTypes[i]).isAssignableFrom(methodParameterTypes[i]);
			}

			// Verify result value produced by the delegate is returned untouched
			assertThat(result).isEqualTo(mockedReturnValue);
		} catch (AssertionError e) {
			throw new AssertionError("Method " + method.getName() + " does not follow the delegation pattern!", e);
		}
	}
}
