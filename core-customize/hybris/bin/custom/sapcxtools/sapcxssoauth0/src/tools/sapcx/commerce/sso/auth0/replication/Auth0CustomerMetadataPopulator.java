package tools.sapcx.commerce.sso.auth0.replication;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashMap;
import java.util.Map;

import com.auth0.json.mgmt.users.User;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class Auth0CustomerMetadataPopulator implements Populator<CustomerModel, User> {
	private String metadataPrefix;
	private String metadataKeyFormat;

	public Auth0CustomerMetadataPopulator(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
		this.metadataKeyFormat = isBlank(metadataPrefix) ? "%1$s" : "%0$s_%1$s";
	}

	@Override
	public void populate(CustomerModel source, User target) throws ConversionException {
		emptyIfNull(source.getAddresses()).stream()
				.filter(this::isContactAddress)
				.findFirst()
				.map(this::convertAddressToMetadata)
				.ifPresent(target::setAppMetadata);
	}

	protected boolean isContactAddress(AddressModel address) {
		return isTrue(address.getContactAddress());
	}

	private Map<String, Object> convertAddressToMetadata(AddressModel address) {
		Map<String, Object> metadata = new HashMap<>(16);
		addStandardContactFields(address, metadata);
		addStandardAddressFields(address, metadata);
		addStandardTelecommunicationFields(address, metadata);
		return metadata;
	}

	protected void addStandardContactFields(AddressModel address, Map<String, Object> metadata) {
		if (address.getTitle() != null) {
			metadata.put(getKey("contact_title"), address.getTitle().getCode());
		}
		if (address.getCompany() != null) {
			metadata.put(getKey("contact_company"), address.getCompany());
		}
		if (address.getDepartment() != null) {
			metadata.put(getKey("contact_department"), address.getDepartment());
		}
	}

	protected void addStandardAddressFields(AddressModel address, Map<String, Object> metadata) {
		if (address.getLine1() != null) {
			metadata.put(getKey("contact_streetname"), address.getLine1());
		}
		if (address.getLine2() != null) {
			metadata.put(getKey("contact_streetnumber"), address.getLine2());
		}
		if (address.getBuilding() != null) {
			metadata.put(getKey("contact_building"), address.getBuilding());
		}
		if (address.getAppartment() != null) {
			metadata.put(getKey("contact_appartment"), address.getAppartment());
		}
		if (address.getPostalcode() != null) {
			metadata.put(getKey("contact_postalcode"), address.getPostalcode());
		}
		if (address.getTown() != null) {
			metadata.put(getKey("contact_city"), address.getTown());
		}
		if (address.getDistrict() != null) {
			metadata.put(getKey("contact_district"), address.getDepartment());
		}
		if (address.getRegion() != null) {
			metadata.put(getKey("contact_region"), address.getRegion().getIsocodeShort());
		}
		if (address.getCountry() != null) {
			metadata.put(getKey("contact_country"), address.getCountry().getIsocode());
		}
		if (address.getPobox() != null) {
			metadata.put(getKey("contact_pobox"), address.getPobox());
		}
	}

	protected void addStandardTelecommunicationFields(AddressModel address, Map<String, Object> metadata) {
		if (address.getCellphone() != null) {
			metadata.put(getKey("contact_cellphone"), address.getCellphone());
		}
		if (address.getPhone1() != null) {
			metadata.put(getKey("contact_phone1"), address.getPhone1());
		}
		if (address.getPhone2() != null) {
			metadata.put(getKey("contact_phone2"), address.getPhone2());
		}
		if (address.getFax() != null) {
			metadata.put(getKey("contact_fax"), address.getFax());
		}
	}

	private String getKey(String fieldName) {
		return format(metadataKeyFormat, metadataPrefix, fieldName);
	}
}
