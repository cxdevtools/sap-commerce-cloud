package tools.sapcx.commerce.toolkit.email;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.mail.internet.InternetAddress;

public class HtmlEmailAddress {
	private String name;
	private String email;

	public static HtmlEmailAddress address(String email) {
		HtmlEmailAddress address = new HtmlEmailAddress();
		address.name = null;
		address.email = email;
		return address;
	}

	public static HtmlEmailAddress address(String name, String email) {
		HtmlEmailAddress address = new HtmlEmailAddress();
		address.name = name;
		address.email = email;
		return address;
	}

	public static HtmlEmailAddress fromInternetAddress(InternetAddress internetAddress) {
		HtmlEmailAddress address = new HtmlEmailAddress();
		address.name = internetAddress.getPersonal();
		address.email = internetAddress.getAddress();
		return address;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("email", email)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		HtmlEmailAddress that = (HtmlEmailAddress) o;
		return new EqualsBuilder()
				.append(name, that.name)
				.append(email, that.email)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(name)
				.append(email)
				.toHashCode();
	}
}
