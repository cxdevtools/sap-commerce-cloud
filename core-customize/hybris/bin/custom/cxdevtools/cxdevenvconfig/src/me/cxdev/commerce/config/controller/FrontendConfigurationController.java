package me.cxdev.commerce.config.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import me.cxdev.commerce.config.FrontendConfigurationWsDto;
import me.cxdev.commerce.config.frontend.FrontendConfigurationService;

@RestController
@RequestMapping(value = "/{baseSiteId}/configuration")
public class FrontendConfigurationController {
	@Resource
	private FrontendConfigurationService frontendConfigurationService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public FrontendConfigurationWsDto environmentSpecificFrontendConfiguration() {
		FrontendConfigurationWsDto configuration = new FrontendConfigurationWsDto();
		configuration.setEnvironmentId(frontendConfigurationService.getEnvironmentId());
		configuration.setEnvironmentName(frontendConfigurationService.getEnvironmentName());
		configuration.setConfig(frontendConfigurationService.getFrontendConfiguration());
		return configuration;
	}

	public void setFrontendConfigurationService(FrontendConfigurationService frontendConfigurationService) {
		this.frontendConfigurationService = frontendConfigurationService;
	}
}
