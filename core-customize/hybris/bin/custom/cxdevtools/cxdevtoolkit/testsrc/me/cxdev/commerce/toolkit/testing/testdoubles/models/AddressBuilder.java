package me.cxdev.commerce.toolkit.testing.testdoubles.models;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.TitleModel;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

public class AddressBuilder {
	public static AddressBuilder emptyAddress() {
		return new AddressBuilder();
	}

	public static AddressBuilder sampleAddress() {
		return emptyAddress()
				.title("Ms")
				.name("First", "Last")
				.streetAndTown("Street", "1", "12345", "Town")
				.country("DE", "Germany")
				.phone("+49 40 12345678")
				.cellphone("+49 160 1234567")
				.email("email@me.com")
				.company("Company AG")
				.district("South");
	}

	public static AddressBuilder sampleShippingAddress() {
		return sampleAddress().street("Shipping Street", "10").asShippingOnly();
	}

	public static AddressBuilder sampleBillingAddress() {
		return sampleAddress().street("Billing Street", "20").asBillingOnly();
	}

	private AddressModel address;

	private AddressBuilder() {
		address = InMemoryModelFactory.createTestableItemModel(AddressModel.class);
	}

	/**
	 * Creates an new {@link AddressModel} based on the current configuration of the builder.
	 * The state of the builder stays untouched, i.e. you can continue to use it and creating
	 * additional addresses with it.
	 *
	 * @return a new copy of a configured address
	 */
	public AddressModel asModel() {
		return InMemoryModelFactory.copy(address);
	}

	public AddressBuilder withoutTitle() {
		address.setTitle(null);
		return this;
	}

	public AddressBuilder title(String title) {
		TitleModel titleModel = InMemoryModelFactory.createTestableItemModel(TitleModel.class);
		titleModel.setCode(title);
		titleModel.setName(title);
		address.setTitle(titleModel);
		return this;
	}

	public AddressBuilder company(String company) {
		address.setCompany(company);
		return this;
	}

	public AddressBuilder name(String first, String last) {
		address.setFirstname(first);
		address.setLastname(last);
		return this;
	}

	public AddressBuilder street(String streetName, String streetNumber) {
		address.setStreetname(streetName);
		address.setStreetnumber(streetNumber);
		return this;
	}

	public AddressBuilder town(String postalcode, String town) {
		address.setPostalcode(postalcode);
		address.setTown(town);
		return this;
	}

	public AddressBuilder streetAndTown(String streetName, String streetNumber, String postalcode, String town) {
		address.setStreetname(streetName);
		address.setStreetnumber(streetNumber);
		address.setPostalcode(postalcode);
		address.setTown(town);
		return this;
	}

	public AddressBuilder withoutCountry() {
		address.setCountry(null);
		return this;
	}

	public AddressBuilder country(String isocode, String countryName) {
		CountryModel countryModel = InMemoryModelFactory.createTestableItemModel(CountryModel.class);
		countryModel.setIsocode(isocode);
		countryModel.setName(countryName);
		address.setCountry(countryModel);
		return this;
	}

	public AddressBuilder region(String isocode, String regionName) {
		RegionModel regionModel = InMemoryModelFactory.createTestableItemModel(RegionModel.class);
		regionModel.setIsocode(isocode);
		regionModel.setName(regionName);
		address.setRegion(regionModel);
		return this;
	}

	public AddressBuilder phone(String phone) {
		address.setPhone1(phone);
		return this;
	}

	public AddressBuilder cellphone(String cellphone) {
		address.setCellphone(cellphone);
		return this;
	}

	public AddressBuilder email(String email) {
		address.setEmail(email);
		return this;
	}

	public AddressBuilder district(String district) {
		address.setDistrict(district);
		return this;
	}

	public AddressBuilder asBillingOnly() {
		address.setBillingAddress(Boolean.TRUE);
		address.setShippingAddress(Boolean.FALSE);
		return this;
	}

	public AddressBuilder asShippingOnly() {
		address.setBillingAddress(Boolean.FALSE);
		address.setShippingAddress(Boolean.TRUE);
		return this;
	}

	public AddressBuilder asBillingAndShipping() {
		address.setBillingAddress(Boolean.FALSE);
		address.setShippingAddress(Boolean.TRUE);
		return this;
	}

	public AddressBuilder isContact(Boolean state) {
		address.setContactAddress(state);
		return this;
	}
}
