package me.cxdev.commerce.toolkit.email;

import static org.assertj.core.api.Assertions.assertThat;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Before;
import org.junit.Test;

@UnitTest
public class HtmlEmailAddressTests {
	private HtmlEmailAddress address;
	private HtmlEmailAddress otherAddress;
	private HtmlEmailAddress sameAddress;

	@Before
	public void setUp() {
		address = HtmlEmailAddress.address("name", "email@localhost");
		otherAddress = HtmlEmailAddress.address("other", "other@localhost");
		sameAddress = HtmlEmailAddress.address("name", "email@localhost");
	}

	@Test
	public void testToString() {
		assertThat(address.toString()).contains("name=name", "email=email@localhost");
		assertThat(otherAddress.toString()).contains("name=other", "email=other@localhost");
		assertThat(sameAddress.toString()).contains("name=name", "email=email@localhost");
	}

	@Test
	public void testHashCode() {
		assertThat(address.hashCode()).isEqualTo(-922879761);
		assertThat(otherAddress.hashCode()).isEqualTo(1061622428);
		assertThat(sameAddress.hashCode()).isEqualTo(-922879761);
	}

	@Test
	public void testEquals() {
		assertThat(address.equals(null)).isFalse();
		assertThat(address.equals(address)).isTrue();
		assertThat(address.equals(otherAddress)).isFalse();
		assertThat(address.equals(sameAddress)).isTrue();
	}
}
