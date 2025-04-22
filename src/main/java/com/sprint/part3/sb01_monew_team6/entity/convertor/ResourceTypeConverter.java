package com.sprint.part3.sb01_monew_team6.entity.convertor;

import com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResourceTypeConverter implements AttributeConverter<ResourceType, String> {

	@Override
	public String convertToDatabaseColumn(ResourceType resourceType) {
		return resourceType == null ? null : resourceType.name().toLowerCase();
	}

	@Override
	public ResourceType convertToEntityAttribute(String attribute) {
		return attribute == null ? null : ResourceType.valueOf(attribute.toUpperCase());
	}
}
