# SAP Commerce Toolkit

The `sapcommercetoolkit` extension improves the SAP Commerce developer experience by offering a number of helpful functions optimizing the
maintenance & operation of the platform, including data imports (essential, initial, sample & test), handling of emails, and a feature to
run unit tests without bootstrapping the platform, incl. a series of test doubles and builders that make writing unit tests easier.

## Model Service Exception Logging
The model service itself does not log its errors and exceptions, which typically is fine, as those are handled or logged in the call stack.
Nevertheless, there are situations in which this is difficult and hard to operate, e.g. in case of running a catalog synchronisation. Due
to missing logging statements within the catalog sync worker, no errors are logged and it is almost impossible to find and analyse errors.

With this implementation, the logging can be enhanced to print all errors and exceptions with level `DEBUG`.  

### How to activate and use
Example: To enable better logging for catalog synchronization failures, one has to do the following steps:

1) Activate the spring profile 'sapcommercetools-modelservice-failurelogging'
2) Reduce the logging level of the following loggers to DEBUG
- `tools.sapcx.commerce.toolkit.model.FailureLoggingModelService`
- `de.hybris.platform.servicelayer.internal.model.impl.DefaultModelService`
3) Set the following properties:
- `synchronization.itemcopycreator.stacktraces=true`
- `cronjob.logtofile.threshold=DEBUG`
4) Then restart your instance and sync to see what's happening under the hood.

## Optimized system setup

The system setup mechanism makes use of the platform properties, that can be extended with by extension. The core principle is, that you 
no longer have to provide your own annotated `@SystemSetup` class, but that you can contribute to a centralized system setup by defining 
properties that follow a convention. Any extension in your project may contribute to the system setup process at different stages:

| Stage         | Description                                                                                                                                                                                                     |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Elementary    | Imported only during initialization. This stage should contain import that are crucial for the system to work.                                                                                                  |
| Release Patch | Only imported once per system during the system update. The `SystemSetup` keeps track of the imported files and the current release version.                                                                    |
| Essential     | Always imported during initialization or update. This contains data that is maintained by the development team and needs to be updated with every release.                                                      |
| Overlay       | Always imported during project data update. This contains data that overlays essentialdata imports from the standard extensions shipped by SAP.                                                                 |
| Sample Data   | Only imported if activated or selected manually in the admin console. This contains data that is shipped by the development team but will be maintained on the platform, e.g. initial CMS pages and components. |
| Test Data     | Only imported if activated or selected manually in the admin console. This contains data that is shipped and used primarly by the development team on local and DEV environments.                               |

Note: We recommend to deactivate the legacy system setup by setting the property `legacysystemsetup=false` in your `local.properties` file.
You need to trigger the system setup process from within the system setup class of your project's core extension. This will ensure that all
dependencies to SAP extensions are resolved correctly and you have the chance to overlay any impex configuration and import performed by the 
default SAP extensions.

### Configuration parameters

| Parameter                                                        | Type    | Description                                                                                                                                                                                                           |
|------------------------------------------------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| sapcommercetoolkit.impeximport.configuration.legacymode          | Boolean | Flag for running all impex imports in legacy mode<br/>(default: `false`)                                                                                                                                              | 
| sapcommercetoolkit.impeximport.configuration.enablecodeexecution | Boolean | Flag for running all impex imports with code execution<br/>(default: `true`)                                                                                                                                          | 
| sapcommercetoolkit.impeximport.configuration.validationmode      | String  | Validation mode for running impex imports<br/>(default: `strict`)                                                                                                                                                     | 
| sapcommercetoolkit.impeximport.configuration.defaultlocale       | String  | Default locale for running impex imports<br/>(default: `en`)                                                                                                                                                          |
| sapcommercetoolkit.impeximport.environment.legacysystemsetup     | Booelan | Define if the legacy system setup mode shall be used. With legacy mode, the system setup is executed from within this extension. Otherwise, the project needs to trigger the system setup.<br/>(default: `true`)      | 
| sapcommercetoolkit.impeximport.environment.configurationfile     | String  | Path to the configuration file that is generated and maintained by the extension. This path must be shared between all cluster nodes!<br/>(default: `${HYBRIS_DATA_DIR}/sapcommercetoolkit/configuration.properties`) | 
| sapcommercetoolkit.impeximport.environment.isdevelopment         | Boolean | Flag for development environments. If an environment is flagged as development, all sample data and test data imports are performed.<br/>(default: `false`)                                                           |
| sapcommercetoolkit.impeximport.environment.supportlocalizedfiles | Boolean | Add support for localized files for all activated languages.<br/>(default: `false`)                                                                                                                                   |
| sapcommercetoolkit.impeximport.environment.importsampledata      | Boolean | If the flag is set to true, sample data imports are performed on this environment.<br/>(default: `false`)                                                                                                             |
| sapcommercetoolkit.impeximport.environment.importtestdata        | Boolean | If the flag is set to true, test data imports are performed on this environment.<br/>(default: `false`)                                                                                                               |

### How to activate and use

The feature itself is activated immediately, but it does not perform any operation without your custom configuration.

Import files can be specified in any property file that is considered as a SAP CX configuration location, e.g.:
- properties area of manifest.json file
- global local.properties file
- extension specific project.properties file

The pattern needs to apply to the following rule: `sapcommercetoolkit.impeximport.<type>.[<version>.]<order>.<name>`
- `<type>`     must be one of the stages mentioned above
- `<version>`  only required for type releasepatch! It must identify the release version with alphanumeric ordering. In other words you 
               must guarantee that the order or the release versions is correct, as the mechanism relies on alphanumerical order.
- `<order>`    level for alphanumerical ordering of impex scripts of the same stage, e.g. use markers like `0100` etc.
- `<name>`     identifier for the import (can be any unique number or text)

#### Sample configuration

```properties
sapcommercetoolkit.impeximport.elementarydata.0100.coredata=/path/to/file.impex
sapcommercetoolkit.impeximport.elementarydata.0500.catalogs=/path/to/file.impex
sapcommercetoolkit.impeximport.releasepatch.release1x0x0.0001.datamigration=/path/to/file.impex
sapcommercetoolkit.impeximport.releasepatch.release1x1x0.0001.datamigration=/path/to/file.impex
sapcommercetoolkit.impeximport.releasepatch.release2x0x0.0001.insertdefaultvalue=/path/to/file.impex
sapcommercetoolkit.impeximport.essentialdata.0010.userrights=/path/to/file.impex
sapcommercetoolkit.impeximport.essentialdata.0300.solrconfiguration=/path/to/file.impex
sapcommercetoolkit.impeximport.essentialdata.5000.cmstemplates=/path/to/file.impex
sapcommercetoolkit.impeximport.overlay.1000.core=/path/to/file.impex
sapcommercetoolkit.impeximport.sampledata.0100.categories=/path/to/file.impex
sapcommercetoolkit.impeximport.sampledata.0200.classificationsystem=/path/to/file.impex
sapcommercetoolkit.impeximport.sampledata.0500.products=/path/to/file.impex
sapcommercetoolkit.impeximport.sampledata.1000.users=/path/to/file.impex
sapcommercetoolkit.impeximport.sampledata.5000.cms=/path/to/file.impex
sapcommercetoolkit.impeximport.testdata.0100.categories=/path/to/file.impex
sapcommercetoolkit.impeximport.testdata.0500.products=/path/to/file.impex
sapcommercetoolkit.impeximport.testdata.1000.users=/path/to/file.impex
sapcommercetoolkit.impeximport.testdata.5000.cms=/path/to/file.impex
```

### Hints

One should include the `sapcommercetoolkit` within the list of extension to execute projectdata updates on
system init and update via the property `update.executeProjectData.extensionName.list=sapcommercetoolkit`.

Typically, one activates the `sapcommercetoolkit.impeximport.environment.supportlocalizedfiles` by setting it to `true`.
This will automatically resolve localized files that have the same name as the one specified in the configuration, but with
a suffix of the locale before the file extension, e.g. for a configuration of `/path/to/file.impex` and a system supporting
the locales (en, de, it) it also resolves the following pathes and tries to import them after the main file:
- `/path/to/file_en.impex`
- `/path/to/file_de.impex`
- `/path/to/file_it.impex`

In addition, sometimes it is necessary to declare a "cleanup" script for the data, e.g. for `CronJob` items. This is also
supported by placing a file with a suffix of `_cleanup` next to the main script. This file will always be executed before
the main script, e.g.: `/path/to/file_cleanup.impex`.

If you need additional stages in your project, you can add them in the spring configuration of your own extension. Please have a look at
the configuration file `systemsetup-spring.xml` and inspect the project data importer beans. They will guide you directly how to do it.



## Centralized HtmlEmail handling

The main purpose of these services is, to provide a simple way of sending emails to the customers, without making use of the `CMSComponents`
like with the standard way of mailing within the SAP Commerce Cloud, provided by the `acceleratorservices` extension. The implementation
makes use of the [Thymeleaf rendering engine](https://www.thymeleaf.org/), i.e. you are able to define your mails as thymeleaf templates
and provide localized messages to it.

The `HtmlEmailGenerator` services (registered as bean with name `thymeleafHtmlEmailGenerator` and alias `htmlEmailGenerator`) should be used
to create `HtmlEmail` objects, whenever you want to send an email, e.g. from Workflows or from EventListeners. The class provides a simple 
and an enhanced mechanism to create `HtmlEmail` objects. The simple way takes a `String` as a body and sets it as HTML body for the email.
The enhanced mechanism takes a template name and context parameters. The template is resolves from the classpath and the template engine is
executed with the provided context parameters.

### How to activate and use

The services are added to the global application context automatically, but they do not replace, nor do they overload services from the
platform without your custom configuration. In order to use the services, you can add them to your services just like any other bean and
call them.

All your template must be placed within the classpath, typically in your `resources` folder at: `resources/email-templates/html` or
`resources/email-templates/text`. If the resolver does not find your template in one of these folders within your classpath it assumes that
the given `String` represents a dynamic template and uses the string as input for the template engine.

Your localized messages must be added to a message bundled called `messages` placed at `resources/email-templates/messages.properties`.

If you are not comfortable with these default configuration, you can specify your own configuration by overlaying the bean alias of the
template engine called `emailTemplateEngine`. 

For local development there is also a `StoreLocallyHtmlEmailService`. This service does not even send any emails, but instead
stores them in a configurable local directory or the database. In order to activate this feature, you need to activate/add the
spring profile `sapcommercetools-fake-localmails` to your `local.properties`:

```properties
spring.profiles.active=sapcommercetools-fake-localmails
```

### Configuration parameters

| Parameter                                                               | Type   | Description                                                                                                                                                                                 |
|-------------------------------------------------------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.method           | String | The method for storing mails locally, either `file` or `database`<br/>(default: `file`)                                                                                                     | 
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.directory        | String | The directory to which the email files will be stored to<br/>(default: `${HYBRIS_LOG_DIR}/mails`)                                                                                           | 
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.filenamepattern  | String | The pattern for the generated files. It can be adjusted with the following parameters: timestamp, datetime, subject, from, to, extension<br/>(default: `{timestamp}_{subject}.{extension}`) | 
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.extension        | String | Specify the file extension for the generated local files, use whatever is supported by your email client<br/>(default: `eml`)                                                               | 
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.mediafolder      | String | The media folder to place fake email media items into<br/>(default: `fake-emails`)                                                                                                          | 
| sapcommercetoolkit.fakes.htmlEmailService.localstorage.daysToKeepEmails | int    | the number of days to keep local emails in the database<br/>(default: `7`)                                                                                                                  | 

### Cleanup of stored email within database

If you store the emails within the database, make sure you are initializing the database from time to time (e.g. on local
development machines), or to setup a maintenance cronjob that removes the fake emails periodically (e.g. on STAGE). The 
sapcommercetoolkit already defines a `cleanupLocallyHtmlEmailsPerformable` bean instance that can be configured by creating
a `CronJob` instance with the following configuration:

```impex
# Clean Up CronJob
INSERT_UPDATE CronJob; code[unique = true]    ; job(code)                  ; sessionLanguage(isoCode)
                     ; cleanupLocallyHtmlEmailsCronJob ; cleanupLocallyHtmlEmailsPerformable ; en

# Trigger for Clean Up
INSERT_UPDATE Trigger; cronJob(code)[unique = true]    ; active; activationTime[dateformat = dd.MM.yyyy HH:mm:ss]; year; month; day; hour; minute; second; relative; weekInterval; daysOfWeek(code)
                     ; cleanupLocallyHtmlEmailsCronJob ; true  ; 01.01.2022 01:00:00                             ; -1  ; -1   ; -1 ; 1   ; 0     ; 0     ; false   ; 1           ; MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
```

**Note**: Keep in mind, that SAP Commerce is creating the `Job` items automatically on system update for each bean
implementing the `JobPerformable` interface. After activating the fake you need to run a system update (or initialize)
first, otherwise the lines above will fail telling you that the `cleanupLocallyHtmlEmailsPerformable` cannot be resolved.

### Extended configuration of the `HtmlEmailService`

This extension does not declare any dependencies to the SAP extension modules. Therefore, if you
want to have the `HtmlEmailService` being responsible for sending all of your mails, you need to
tweak the standard modules.

SAP makes use of Apache `commons-email` library. The `HtmlEmail#send()` method will directly execute
the send command by using the parameters set to the object. The `HtmlEmailService` provides a `proxy`
method, that will use the CGLIB library to enhance the `HtmlEmail`. With this enhancement, the proxy
will call the `HtmlEmailService#sendEmail(HtmlEmail)` method, which then will invoke the `HtmlEmail#send()'
method.

For your own code and extensions, we recommend to make use of the `HtmlEmailGenerator` and `HtmlEmailService`
directly, obeying the logic from the SAP standard. To activate the proxy for standard modules, you have
to provide some overlaying beans:

1. If you make use of acceleratorservices module, create a class `MyEmailService` with the following implementaion:
```java
public class MyEmailService extends DefaultEmailService {
  @Resource private HtmlEmailGenerator htmlEmailGenerator;
  @Resource private HtmlEmailService htmlEmailService;

  @Override protected HtmlEmail getPerConfiguredEmail() throws EmailException {
    return htmlEmailService.proxy(htmlEmailGenerator.createHtmlEmail());
  }
}
```

2. If you make use of b2bapprovalprocess module, create a class `MyB2BEmailService` with the following implementation:

```java
public class MyB2BEmailService implements B2BEmailService {
  @Resource(name = "defaultB2BEmailService") private B2BEmailService delegate;
  @Resource private HtmlEmailService htmlEmailService;

  @Override public void sendEmail(HtmlEmail email) throws EmailException {
    htmlEmailService.sendEmail(email);
  }

  @Override public HtmlEmail createOrderApprovalEmail(String emailTemplateCode, OrderModel order, B2BCustomerModel user, InternetAddress from, String subject) throws EmailException {
    return htmlEmailService.proxy(delegate.createOrderApprovalEmail(emailTemplateCode, order, user, from, subject));
  }

  @Override public HtmlEmail createOrderRejectionEmail(String emailTemplateCode, OrderModel order, B2BCustomerModel user, InternetAddress from, String subject) throws EmailException {
    return htmlEmailService.proxy(delegate.createOrderRejectionEmail(emailTemplateCode, order, user, from, subject));
  }
}
```

3. Extend your spring context configuration with the following beans and aliases (or parts of them, depending on the modules in use):

```xml
<!-- Overlay of EmailService to make use of htmlEmailService -->
<alias name="myEmailService" alias="emailService"/>
<bean id="myEmailService" class="tools.sapcx.commerce.samples.mail.MyEmailService" parent="defaultEmailService">
  <property name="htmlEmailGenerator" ref="htmlEmailGenerator"/>
  <property name="htmlEmailService" ref="htmlEmailService"/>
</bean>

<!-- Overlay of B2BEmailService to make use of htmlEmailService -->
<alias name="myB2BEmailService" alias="b2bEmailService"/>
<bean id="myB2BEmailService" class="tools.sapcx.commerce.samples.mail.MyB2BEmailService">
  <property name="delegate" ref="defaultB2BEmailService"/>
  <property name="htmlEmailService" ref="htmlEmailService"/>
</bean>

```

These overlays do not influence the behavior or the functionality of the SAP standard. They simply guarantee that the email sending process
will be controlled by the `HtmlEmailService`, allowing the fake service to take over control and store all emails locally. 



## Unit testing and test doubles

Unit testing in SAP Commerce has a downside when it comes to test services that operate on `AbstractItemModel` objects. Typically, the
items are bound to the application context, because they rely on the `PK` class that generates a `UUID` for them. In order to do so, the
`PK` class uses the `DefaultPKCounterGenerator` that triggers `Registry.getCurrentTenant().getSerialNumberGenerator()`. This request then
starts the server and leads to very long unit test cycles.

In order to avoid this, many projects (and even SAP) make extensively use of [Mockito](https://site.mockito.org/) and mock the
`AbstractItemModel` classes. This works fine, but it adds a tremendous overhead to unit test writing, because all setters and getters need
to be mocked and specified. To avoid this, the `InMemoryModelFactory` was invented and introduced. It gives the ability to create
`AbstractItemModel` classes that make use of an `InMemoryModelContext` storing all attributes and their values in a HashMap. For sure,
this has some limitations, but for the purpose of unit testing with `AbstractItemModel` objects involved, this clearly helps a lot.

### How to activate and use

All the unit testing enhancements are placed within the `testsrc` folder. Therefore, they are activated by default, but only enhance your
test execution, not your production system. If you want to make use of the testing capabilities, you need to declare a dependency to the
`sapcommercetoolkit` extension to your extension. This dependency can also be transient, e.g. normally the dependency is only added to the
`core` extensions `extensioninfo.xml` file of the project and is then available to all extensions within the project:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<extensioninfo xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="extensioninfo.xsd">
    <extension abstractclassprefix="Generated" classprefix="MyCore" name="mycore">
        <!-- your requires-extension list goes here -->
        
        <!-- Use SAP Commerce Toolkit -->
        <requires-extension name="sapcommercetoolkit"/>
        
        <coremodule generated="true" manager="de.hybris.platform.jalo.extension.GenericManager" packageroot="tools.sapcx.commerce.samples.core"/>
    </extension>
</extensioninfo>
```

In your unit tests, you can then simply create instances of `ItemModel` classes by invoking the `InMemoryModelFactory.create(Class)` method
or by using the `InMemoryModelServiceFake`, if your code relies on using the `ModelService` interface for generating `ItemModel` instances:

```java
@UnitTest
public class Sample1Tests {
    private CustomerModel customerModel;

    private MyPrepareInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        customerModel = InMemoryModelFactory.createTestableItemModel(CustomerModel.class);
        customerModel.setUid("test-customer@local.dev");
        customerModel.setName("Test Customer");
        customerModel.setEmail("test-customer@local.dev");
        customerModel.setLoginDisabled(Boolean.FALSE);

        interceptor = new MyPrepareInterceptor();
    }

    @Test
    public void someVerificationStep() throws InterceptorException {
        InterceptorContext context = interceptorContext().withNew(true).stub();
        interceptor.onPrepare(customerModel, context);

        assertThat(customerModel.isLoginDisabled()).isTrue();
        assertThat(customerModel.getToken()).isNotEmpty();
    }
}

@UnitTest
public class Sample2Tests {
    private ModelSerivce modelService;

    private MyService service;

    @Before
    public void setUp() throws Exception {
        modelService = new InMemoryModelSeriveFake();
        service = new MyService(modelService);
    }

    @Test
    public void someVerificationStep() throws InterceptorException {
        MyItemModel item = service.executeLogicToCreateAnItemModel();
        
        assertThat(item.somePropertyToVerify()).isTrue();
        assertThat(item.someOtherAspectToVerify()).isNotEmpty();
    }
}
```
### Test doubles and builders

In the samples above you might have wondered where this `interceptorContext()` line comes from. This and some other builders shall help to
make unit testing with SAP Commerce much easier. The library is still growing and sometimes the test doubles are not finally there, but we
have the goal that one day it has grown to a size big enough to support you in the major use cases throughout the platform.

Test doubles and their builders are placed within the `testsrc` folder within the package `tools.sapcx.commerce.toolkit.testing` and
below. The naming convention is, that the test doubles should always start with the interface name they are supporting, e.g.:
- `CatalogVersionService` => `CatalogVersionServiceFake`
- `ConfigurationService` => `ConfigurationServiceFake`
- `ModelService` => `ModelServiceFake` or `ModelServiceSpy`
- `EventService` => `EventServiceFake` or `EventServiceSpy`

For sure, you can always use Mockito to create your stubs, mocks and spies, but be aware that creating the `given().when().then()` chains 
will make your test code hard to read or understand, and hard to maintain and reuse! The ladder is the most critical topic here. Having
your own stubs, mocks, spies and fakes you have a great ability to control them and provide builder on them to support typical setups.

We will not explain every single detail here in the README. Feel free to search the `testsrc` for existing builders and play around with
them. And if you are missing a builder, feel free to raise an issue or to create a pull request for it. We are happy for any support.



## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2025, SAP CX Tools_