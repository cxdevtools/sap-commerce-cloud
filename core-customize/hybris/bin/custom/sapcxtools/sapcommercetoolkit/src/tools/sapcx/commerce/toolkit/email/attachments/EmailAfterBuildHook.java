package tools.sapcx.commerce.toolkit.email.attachments;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

@FunctionalInterface
public interface EmailAfterBuildHook {
	void afterBuild(HtmlEmail email) throws EmailException;
}
