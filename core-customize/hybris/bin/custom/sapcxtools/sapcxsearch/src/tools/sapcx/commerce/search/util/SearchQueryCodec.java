package tools.sapcx.commerce.search.util;

/**
 * INFO: This interface is an exact copy of de.hybris.platform.commercewebservices.core.util.ws.SearchQueryCodec<QUERY>
 * This was done so that we have this interface accessible to our Facade Layer classes (while the Original
 * is placed in the WebContext and hence is not reachable from Facade Layer)
 */
public interface SearchQueryCodec<QUERY> {
	QUERY decodeQuery(String query);

	String encodeQuery(QUERY query);
}
