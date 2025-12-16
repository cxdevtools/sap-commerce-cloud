package tools.sapcx.commerce.toolkit.email;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.Email;
import org.apache.commons.mail2.jakarta.EmailAttachment;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.apache.commons.mail2.jakarta.MultiPartEmail;

import jakarta.activation.DataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

class ProxyHtmlEmail extends HtmlEmail {
	private static final String UNSUPPORTED_OPERATION_MESSAGE = "This method must not be called on the proxy object!";

	private HtmlEmailService htmlEmailService;
	private HtmlEmail proxiedHtmlEmail;

	public <T extends HtmlEmail> ProxyHtmlEmail(HtmlEmailService htmlEmailService, T proxiedHtmlEmail) {
		this.htmlEmailService = htmlEmailService;
		this.proxiedHtmlEmail = proxiedHtmlEmail;
	}

	@Override
	public String send() throws EmailException {
		return htmlEmailService.sendEmail(proxiedHtmlEmail);
	}

	public HtmlEmail getProxiedHtmlEmail() {
		return proxiedHtmlEmail;
	}

	@Override
	public HtmlEmail setTextMsg(String aText) throws EmailException {
		return proxiedHtmlEmail.setTextMsg(aText);
	}

	@Override
	public HtmlEmail setHtmlMsg(String aHtml) throws EmailException {
		return proxiedHtmlEmail.setHtmlMsg(aHtml);
	}

	@Override
	public Email setMsg(String msg) throws EmailException {
		return proxiedHtmlEmail.setMsg(msg);
	}

	@Override
	public String embed(String urlString, String name) throws EmailException {
		return proxiedHtmlEmail.embed(urlString, name);
	}

	@Override
	public String embed(URL url, String name) throws EmailException {
		return proxiedHtmlEmail.embed(url, name);
	}

	@Override
	public String embed(File file) throws EmailException {
		return proxiedHtmlEmail.embed(file);
	}

	@Override
	public String embed(File file, String cid) throws EmailException {
		return proxiedHtmlEmail.embed(file, cid);
	}

	@Override
	public String embed(DataSource dataSource, String name) throws EmailException {
		return proxiedHtmlEmail.embed(dataSource, name);
	}

	@Override
	public String embed(DataSource dataSource, String name, String cid) throws EmailException {
		return proxiedHtmlEmail.embed(dataSource, name, cid);
	}

	@Override
	public void buildMimeMessage() throws EmailException {
		proxiedHtmlEmail.buildMimeMessage();
	}

	@Override
	public void setSubType(String aSubType) {
		proxiedHtmlEmail.setSubType(aSubType);
	}

	@Override
	public String getSubType() {
		return proxiedHtmlEmail.getSubType();
	}

	@Override
	public Email addPart(String partContent, String partContentType) throws EmailException {
		return proxiedHtmlEmail.addPart(partContent, partContentType);
	}

	@Override
	public Email addPart(MimeMultipart multipart) throws EmailException {
		return proxiedHtmlEmail.addPart(multipart);
	}

	@Override
	public Email addPart(MimeMultipart multipart, int index) throws EmailException {
		return proxiedHtmlEmail.addPart(multipart, index);
	}

	@Override
	public MultiPartEmail attach(File file) throws EmailException {
		return proxiedHtmlEmail.attach(file);
	}

	@Override
	public MultiPartEmail attach(EmailAttachment attachment) throws EmailException {
		return proxiedHtmlEmail.attach(attachment);
	}

	@Override
	public MultiPartEmail attach(URL url, String name, String description) throws EmailException {
		return proxiedHtmlEmail.attach(url, name, description);
	}

	@Override
	public MultiPartEmail attach(URL url, String name, String description, String disposition) throws EmailException {
		return proxiedHtmlEmail.attach(url, name, description, disposition);
	}

	@Override
	public MultiPartEmail attach(DataSource ds, String name, String description) throws EmailException {
		return proxiedHtmlEmail.attach(ds, name, description);
	}

	@Override
	public MultiPartEmail attach(DataSource ds, String name, String description, String disposition) throws EmailException {
		return proxiedHtmlEmail.attach(ds, name, description, disposition);
	}

	@Override
	public BodyPart getPrimaryBodyPart() throws MessagingException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public MimeMultipart getContainer() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public BodyPart createBodyPart() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public MimeMultipart createMimeMultipart() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	protected void init() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public boolean isBoolHasAttachments() {
		return proxiedHtmlEmail.isBoolHasAttachments();
	}

	@Override
	public void setBoolHasAttachments(boolean b) {
		proxiedHtmlEmail.setBoolHasAttachments(b);
	}

	@Override
	public void setDebug(boolean d) {
		proxiedHtmlEmail.setDebug(d);
	}

	@Override
	public void setAuthentication(String userName, String password) {
		proxiedHtmlEmail.setAuthentication(userName, password);
	}

	@Override
	public void setAuthenticator(Authenticator newAuthenticator) {
		proxiedHtmlEmail.setAuthenticator(newAuthenticator);
	}

	@Override
	public void setCharset(String newCharset) {
		proxiedHtmlEmail.setCharset(newCharset);
	}

	@Override
	public void setContent(MimeMultipart aMimeMultipart) {
		proxiedHtmlEmail.setContent(aMimeMultipart);
	}

	@Override
	public void setContent(Object aObject, String aContentType) {
		proxiedHtmlEmail.setContent(aObject, aContentType);
	}

	@Override
	public void updateContentType(String aContentType) {
		proxiedHtmlEmail.updateContentType(aContentType);
	}

	@Override
	public void setHostName(String aHostName) {
		proxiedHtmlEmail.setHostName(aHostName);
	}

	@Override
	public Email setStartTLSEnabled(boolean startTlsEnabled) {
		return proxiedHtmlEmail.setStartTLSEnabled(startTlsEnabled);
	}

	@Override
	public Email setStartTLSRequired(boolean startTlsRequired) {
		return proxiedHtmlEmail.setStartTLSRequired(startTlsRequired);
	}

	@Override
	public void setSmtpPort(int aPortNumber) {
		proxiedHtmlEmail.setSmtpPort(aPortNumber);
	}

	@Override
	public void setMailSession(Session aSession) {
		proxiedHtmlEmail.setMailSession(aSession);
	}

	@Override
	public void setMailSessionFromJNDI(String jndiName) throws NamingException {
		proxiedHtmlEmail.setMailSessionFromJNDI(jndiName);
	}

	@Override
	public Session getMailSession() throws EmailException {
		return proxiedHtmlEmail.getMailSession();
	}

	@Override
	public Email setFrom(String email) throws EmailException {
		return proxiedHtmlEmail.setFrom(email);
	}

	@Override
	public Email setFrom(String email, String name) throws EmailException {
		return proxiedHtmlEmail.setFrom(email, name);
	}

	@Override
	public Email setFrom(String email, String name, String charset) throws EmailException {
		return proxiedHtmlEmail.setFrom(email, name, charset);
	}

	@Override
	public Email addTo(String email) throws EmailException {
		return proxiedHtmlEmail.addTo(email);
	}

	@Override
	public Email addTo(String... emails) throws EmailException {
		return proxiedHtmlEmail.addTo(emails);
	}

	@Override
	public Email addTo(String email, String name) throws EmailException {
		return proxiedHtmlEmail.addTo(email, name);
	}

	@Override
	public Email addTo(String email, String name, String charset) throws EmailException {
		return proxiedHtmlEmail.addTo(email, name, charset);
	}

	@Override
	public Email setTo(Collection<InternetAddress> aCollection) throws EmailException {
		return proxiedHtmlEmail.setTo(aCollection);
	}

	@Override
	public Email addCc(String email) throws EmailException {
		return proxiedHtmlEmail.addCc(email);
	}

	@Override
	public Email addCc(String... emails) throws EmailException {
		return proxiedHtmlEmail.addCc(emails);
	}

	@Override
	public Email addCc(String email, String name) throws EmailException {
		return proxiedHtmlEmail.addCc(email, name);
	}

	@Override
	public Email addCc(String email, String name, String charset) throws EmailException {
		return proxiedHtmlEmail.addCc(email, name, charset);
	}

	@Override
	public Email setCc(Collection<InternetAddress> aCollection) throws EmailException {
		return proxiedHtmlEmail.setCc(aCollection);
	}

	@Override
	public Email addBcc(String email) throws EmailException {
		return proxiedHtmlEmail.addBcc(email);
	}

	@Override
	public Email addBcc(String... emails) throws EmailException {
		return proxiedHtmlEmail.addBcc(emails);
	}

	@Override
	public Email addBcc(String email, String name) throws EmailException {
		return proxiedHtmlEmail.addBcc(email, name);
	}

	@Override
	public Email addBcc(String email, String name, String charset) throws EmailException {
		return proxiedHtmlEmail.addBcc(email, name, charset);
	}

	@Override
	public Email setBcc(Collection<InternetAddress> aCollection) throws EmailException {
		return proxiedHtmlEmail.setBcc(aCollection);
	}

	@Override
	public Email addReplyTo(String email) throws EmailException {
		return proxiedHtmlEmail.addReplyTo(email);
	}

	@Override
	public Email addReplyTo(String email, String name) throws EmailException {
		return proxiedHtmlEmail.addReplyTo(email, name);
	}

	@Override
	public Email addReplyTo(String email, String name, String charset) throws EmailException {
		return proxiedHtmlEmail.addReplyTo(email, name, charset);
	}

	@Override
	public Email setReplyTo(Collection<InternetAddress> aCollection) throws EmailException {
		return proxiedHtmlEmail.setReplyTo(aCollection);
	}

	@Override
	public void setHeaders(Map<String, String> map) {
		proxiedHtmlEmail.setHeaders(map);
	}

	@Override
	public void addHeader(String name, String value) {
		proxiedHtmlEmail.addHeader(name, value);
	}

	@Override
	public String getHeader(String header) {
		return proxiedHtmlEmail.getHeader(header);
	}

	@Override
	public Map<String, String> getHeaders() {
		return proxiedHtmlEmail.getHeaders();
	}

	@Override
	public Email setSubject(String aSubject) {
		return proxiedHtmlEmail.setSubject(aSubject);
	}

	@Override
	public String getBounceAddress() {
		return proxiedHtmlEmail.getBounceAddress();
	}

	@Override
	public Email setBounceAddress(String email) {
		return proxiedHtmlEmail.setBounceAddress(email);
	}

	@Override
	public String sendMimeMessage() throws EmailException {
		return proxiedHtmlEmail.sendMimeMessage();
	}

	@Override
	public boolean isInitialized() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public void setInitialized(boolean b) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public InternetAddress[] toInternetAddressArray(List<InternetAddress> list) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public MimeMessage createMimeMessage(Session aSession) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public MimeMessage getMimeMessage() {
		return proxiedHtmlEmail.getMimeMessage();
	}

	@Override
	public void setSentDate(Date date) {
		proxiedHtmlEmail.setSentDate(date);
	}

	@Override
	public Date getSentDate() {
		return proxiedHtmlEmail.getSentDate();
	}

	@Override
	public String getSubject() {
		return proxiedHtmlEmail.getSubject();
	}

	@Override
	public InternetAddress getFromAddress() {
		return proxiedHtmlEmail.getFromAddress();
	}

	@Override
	public String getHostName() {
		return proxiedHtmlEmail.getHostName();
	}

	@Override
	public String getSmtpPort() {
		return proxiedHtmlEmail.getSmtpPort();
	}

	@Override
	public boolean isStartTLSRequired() {
		return proxiedHtmlEmail.isStartTLSRequired();
	}

	@Override
	public boolean isStartTLSEnabled() {
		return proxiedHtmlEmail.isStartTLSEnabled();
	}

	@Override
	public void setPopBeforeSmtp(boolean newPopBeforeSmtp, String newPopHost, String newPopUsername, String newPopPassword) {
		proxiedHtmlEmail.setPopBeforeSmtp(newPopBeforeSmtp, newPopHost, newPopUsername, newPopPassword);
	}

	@Override
	public boolean isSSLOnConnect() {
		return proxiedHtmlEmail.isSSLOnConnect();
	}

	@Override
	public Email setSSLOnConnect(boolean ssl) {
		return proxiedHtmlEmail.setSSLOnConnect(ssl);
	}

	@Override
	public boolean isSSLCheckServerIdentity() {
		return proxiedHtmlEmail.isSSLCheckServerIdentity();
	}

	@Override
	public Email setSSLCheckServerIdentity(boolean sslCheckServerIdentity) {
		return proxiedHtmlEmail.setSSLCheckServerIdentity(sslCheckServerIdentity);
	}

	@Override
	public String getSslSmtpPort() {
		return proxiedHtmlEmail.getSslSmtpPort();
	}

	@Override
	public void setSslSmtpPort(String sslSmtpPort) {
		proxiedHtmlEmail.setSslSmtpPort(sslSmtpPort);
	}

	@Override
	public boolean isSendPartial() {
		return proxiedHtmlEmail.isSendPartial();
	}

	@Override
	public Email setSendPartial(boolean sendPartial) {
		return proxiedHtmlEmail.setSendPartial(sendPartial);
	}

	@Override
	public List<InternetAddress> getToAddresses() {
		return proxiedHtmlEmail.getToAddresses();
	}

	@Override
	public List<InternetAddress> getCcAddresses() {
		return proxiedHtmlEmail.getCcAddresses();
	}

	@Override
	public List<InternetAddress> getBccAddresses() {
		return proxiedHtmlEmail.getBccAddresses();
	}

	@Override
	public List<InternetAddress> getReplyToAddresses() {
		return proxiedHtmlEmail.getReplyToAddresses();
	}

	@Override
	public int getSocketConnectionTimeout() {
		return proxiedHtmlEmail.getSocketConnectionTimeout();
	}

	@Override
	public int getSocketTimeout() {
		return proxiedHtmlEmail.getSocketTimeout();
	}
}
