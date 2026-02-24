package me.cxdev.commerce.toolkit.testing.testdoubles.catalog;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeUnitModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.classification.ClassificationService;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.classification.filter.ProductFilter;
import de.hybris.platform.classification.filter.ProductFilterResult;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import org.apache.commons.lang3.StringUtils;

public class ClassificationServiceFake implements ClassificationService {
	private Map<String, List<Feature>> features = new HashMap<>();
	private Collection<ClassificationAttributeUnitModel> units = new ArrayList<>();

	static String key(ClassAttributeAssignmentModel assignment) {
		String systemId = assignment.getSystemVersion().getCatalog().getId();
		String systemVersion = assignment.getSystemVersion().getVersion();
		String classificationCode = assignment.getClassificationClass().getCode();
		String attributeCode = assignment.getClassificationAttribute().getCode();
		return key(systemId, systemVersion, classificationCode, attributeCode);
	}

	static String key(String systemId, String systemVersion, String classificationCode, String attributeCode) {
		return StringUtils.joinWith(":", List.of(systemId, systemVersion, classificationCode, attributeCode));
	}

	@Override
	public Feature getFeature(ItemModel item, ClassAttributeAssignmentModel assignment) {
		return (item instanceof ProductModel) ? getFeature((ProductModel) item, assignment) : null;
	}

	@Override
	public Feature getFeature(ProductModel product, ClassAttributeAssignmentModel assignment) {
		return getFeatures(product).getFeatures().stream()
				.filter(feature -> key(feature.getClassAttributeAssignment()).equals(key(assignment)))
				.findFirst()
				.orElse(null);
	}

	@Override
	public FeatureList getFeatures(ProductModel product) {
		return new FeatureList(features.computeIfAbsent(product.getCode(), (s) -> new ArrayList<>()));
	}

	@Override
	public FeatureList getFeatures(ProductModel product, List<ClassAttributeAssignmentModel> assignments) {
		Set<String> keys = emptyIfNull(assignments).stream()
				.map(ClassificationServiceFake::key)
				.collect(Collectors.toSet());
		return new FeatureList(getFeatures(product).getFeatures().stream()
				.filter(feature -> keys.contains(key(feature.getClassAttributeAssignment())))
				.collect(Collectors.toList()));
	}

	@Override
	public void setFeature(ProductModel product, Feature feature) {
		List<Feature> productFeatures = features.computeIfAbsent(product.getCode(), (s) -> new ArrayList<>());
		productFeatures.add(feature);
	}

	@Override
	public void setFeatures(ProductModel product, FeatureList featureList) {
		List<Feature> productFeatures = features.computeIfAbsent(product.getCode(), (s) -> new ArrayList<>());
		productFeatures.addAll(featureList.getFeatures());
	}

	@Override
	public void replaceFeatures(ProductModel product, FeatureList featureList) {
		List<Feature> productFeatures = features.computeIfAbsent(product.getCode(), (s) -> new ArrayList<>());
		productFeatures.clear();
		productFeatures.addAll(featureList.getFeatures());
	}

	@Override
	public Collection<ClassificationAttributeUnitModel> getAttributeUnits(ClassificationSystemVersionModel systemVersion) {
		return units;
	}

	public void setAttributeUnits(Collection<ClassificationAttributeUnitModel> units) {
		this.units = units;
	}

	@Override
	public ProductFilterResult getProductsByFilter(ProductFilter filter) throws UnknownIdentifierException, AmbiguousIdentifierException {
		throw new UnsupportedOperationException("getProductsByFilter is not supported with this fake");
	}
}
