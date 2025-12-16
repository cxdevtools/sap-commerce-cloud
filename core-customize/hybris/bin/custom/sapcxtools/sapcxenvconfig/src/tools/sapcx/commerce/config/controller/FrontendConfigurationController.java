package tools.sapcx.commerce.config.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tools.sapcx.commerce.config.FrontendConfigurationWsDto;
import tools.sapcx.commerce.config.frontend.FrontendConfigurationService;

import jakarta.annotation.Resource;

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
