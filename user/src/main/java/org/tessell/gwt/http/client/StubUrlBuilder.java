package org.tessell.gwt.http.client;

import java.util.HashMap;
import java.util.Map;

import org.tessell.place.tokenizer.Codec;

public class StubUrlBuilder implements IsUrlBuilder {

  /**
   * The port to use when no port should be specified.
   */
  public static final int PORT_UNSPECIFIED = Integer.MIN_VALUE;

  /**
   * A mapping of query parameters to their values.
   */
  private final Map<String, String[]> listParamMap = new HashMap<String, String[]>();

  private String protocol = "http";
  private String host = null;
  private int port = PORT_UNSPECIFIED;
  private String path = null;
  private String hash = null;

  /**
   * Build the URL and return it as an encoded string.
   * 
   * @return the encoded URL string
   */
  public String buildString() {
    final StringBuilder url = new StringBuilder();

    // http://
    url.append(protocol).append("://");

    // http://www.google.com
    if (host != null) {
      url.append(host);
    }

    // http://www.google.com:80
    if (port != PORT_UNSPECIFIED) {
      url.append(":").append(port);
    }

    // http://www.google.com:80/path/to/file.html
    if (path != null && !"".equals(path)) {
      url.append("/").append(path);
    }

    // Generate the query string.
    // http://www.google.com:80/path/to/file.html?k0=v0&k1=v1
    char prefix = '?';
    for (final Map.Entry<String, String[]> entry : listParamMap.entrySet()) {
      for (final String val : entry.getValue()) {
        url.append(prefix).append(entry.getKey()).append('=');
        if (val != null) {
          url.append(val);
        }
        prefix = '&';
      }
    }

    // http://www.google.com:80/path/to/file.html?k0=v0&k1=v1#token
    if (hash != null) {
      url.append("#").append(hash);
    }

    // NOTE: Only real change from UrlBuilder
    return Codec.encodeURI(url.toString());
  }

  /**
   * Remove a query parameter from the map.
   * 
   * @param name
   *          the parameter name
   */
  @Override
  public IsUrlBuilder removeParameter(final String name) {
    listParamMap.remove(name);
    return this;
  }

  /**
   * Set the hash portion of the location (ex. myAnchor or #myAnchor).
   * 
   * @param hash
   *          the hash
   */
  @Override
  public IsUrlBuilder setHash(String hash) {
    if (hash != null && hash.startsWith("#")) {
      hash = hash.substring(1);
    }
    this.hash = hash;
    return this;
  }

  /**
   * Set the host portion of the location (ex. google.com). You can also specify the port in this method (ex.
   * localhost:8888).
   * 
   * @param host
   *          the host
   */
  @Override
  public IsUrlBuilder setHost(String host) {
    // Extract the port from the host.
    if (host != null && host.contains(":")) {
      final String[] parts = host.split(":");
      if (parts.length > 2) {
        throw new IllegalArgumentException("Host contains more than one colon: " + host);
      }
      try {
        setPort(Integer.parseInt(parts[1]));
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException("Could not parse port out of host: " + host);
      }
      host = parts[0];
    }
    this.host = host;
    return this;
  }

  /**
   * <p>
   * Set a query parameter to a list of values. Each value in the list will be added as its own key/value pair.
   * 
   * <p>
   * <h3>Example Output</h3>
   * <code>?mykey=value0&mykey=value1&mykey=value2</code>
   * </p>
   * 
   * @param key
   *          the key
   * @param values
   *          the list of values
   */
  @Override
  public IsUrlBuilder setParameter(final String key, final String... values) {
    assertNotNullOrEmpty(key, "Key cannot be null or empty", false);
    assertNotNull(values, "Values cannot null. Try using removeParameter instead.");
    if (values.length == 0) {
      throw new IllegalArgumentException("Values cannot be empty.  Try using removeParameter instead.");
    }
    listParamMap.put(key, values);
    return this;
  }

  /**
   * Set the path portion of the location (ex. path/to/file.html).
   * 
   * @param path
   *          the path
   */
  @Override
  public IsUrlBuilder setPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    this.path = path;
    return this;
  }

  /**
   * Set the port to connect to.
   * 
   * @param port
   *          the port, or {@link #PORT_UNSPECIFIED}
   */
  @Override
  public IsUrlBuilder setPort(final int port) {
    this.port = port;
    return this;
  }

  /**
   * Set the protocol portion of the location (ex. http).
   * 
   * @param protocol
   *          the protocol
   */
  @Override
  public IsUrlBuilder setProtocol(String protocol) {
    assertNotNull(protocol, "Protocol cannot be null");
    if (protocol.endsWith("://")) {
      protocol = protocol.substring(0, protocol.length() - 3);
    } else if (protocol.endsWith(":/")) {
      protocol = protocol.substring(0, protocol.length() - 2);
    } else if (protocol.endsWith(":")) {
      protocol = protocol.substring(0, protocol.length() - 1);
    }
    if (protocol.contains(":")) {
      throw new IllegalArgumentException("Invalid protocol: " + protocol);
    }
    assertNotNullOrEmpty(protocol, "Protocol cannot be empty", false);
    this.protocol = protocol;
    return this;
  }

  /**
   * Assert that the value is not null.
   * 
   * @param value
   *          the value
   * @param message
   *          the message to include with any exceptions
   * @throws IllegalArgumentException
   *           if value is null
   */
  private void assertNotNull(final Object value, final String message) throws IllegalArgumentException {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Assert that the value is not null or empty.
   * 
   * @param value
   *          the value
   * @param message
   *          the message to include with any exceptions
   * @param isState
   *          if true, throw a state exception instead
   * @throws IllegalArgumentException
   *           if value is null
   * @throws IllegalStateException
   *           if value is null and isState is true
   */
  private void assertNotNullOrEmpty(final String value, final String message, final boolean isState) throws IllegalArgumentException {
    if (value == null || value.length() == 0) {
      if (isState) {
        throw new IllegalStateException(message);
      } else {
        throw new IllegalArgumentException(message);
      }
    }
  }
}
