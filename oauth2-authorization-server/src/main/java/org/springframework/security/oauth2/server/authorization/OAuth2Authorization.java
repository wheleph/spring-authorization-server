/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.server.authorization;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * A representation of an OAuth 2.0 Authorization, which holds state related to the
 * authorization granted to a {@link #getRegisteredClientId() client}, by the
 * {@link #getPrincipalName() resource owner} or itself in the case of the
 * {@code client_credentials} grant type.
 *
 * @author Joe Grandja
 * @author Krisztian Toth
 * @since 0.0.1
 * @see RegisteredClient
 * @see AuthorizationGrantType
 * @see OAuth2Token
 * @see OAuth2AccessToken
 * @see OAuth2RefreshToken
 */
public class OAuth2Authorization implements Serializable {

	@Serial
	private static final long serialVersionUID = 880363144799377926L;

	private String id;

	private String registeredClientId;

	private String principalName;

	private AuthorizationGrantType authorizationGrantType;

	private Set<String> authorizedScopes;

	private Map<Class<? extends OAuth2Token>, Token<?>> tokens;

	private Map<String, Object> attributes;

	protected OAuth2Authorization() {
	}

	/**
	 * Returns the identifier for the authorization.
	 * @return the identifier for the authorization
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the identifier for the {@link RegisteredClient#getId() registered client}.
	 * @return the {@link RegisteredClient#getId()}
	 */
	public String getRegisteredClientId() {
		return this.registeredClientId;
	}

	/**
	 * Returns the {@code Principal} name of the resource owner (or client).
	 * @return the {@code Principal} name of the resource owner (or client)
	 */
	public String getPrincipalName() {
		return this.principalName;
	}

	/**
	 * Returns the {@link AuthorizationGrantType authorization grant type} used for the
	 * authorization.
	 * @return the {@link AuthorizationGrantType} used for the authorization
	 */
	public AuthorizationGrantType getAuthorizationGrantType() {
		return this.authorizationGrantType;
	}

	/**
	 * Returns the authorized scope(s).
	 * @return the {@code Set} of authorized scope(s)
	 * @since 0.4.0
	 */
	public Set<String> getAuthorizedScopes() {
		return this.authorizedScopes;
	}

	/**
	 * Returns the {@link Token} of type {@link OAuth2AccessToken}.
	 * @return the {@link Token} of type {@link OAuth2AccessToken}
	 */
	public Token<OAuth2AccessToken> getAccessToken() {
		return getToken(OAuth2AccessToken.class);
	}

	/**
	 * Returns the {@link Token} of type {@link OAuth2RefreshToken}.
	 * @return the {@link Token} of type {@link OAuth2RefreshToken}, or {@code null} if
	 * not available
	 */
	@Nullable
	public Token<OAuth2RefreshToken> getRefreshToken() {
		return getToken(OAuth2RefreshToken.class);
	}

	/**
	 * Returns the {@link Token} of type {@code tokenType}.
	 * @param tokenType the token type
	 * @param <T> the type of the token
	 * @return the {@link Token}, or {@code null} if not available
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends OAuth2Token> Token<T> getToken(Class<T> tokenType) {
		Assert.notNull(tokenType, "tokenType cannot be null");
		Token<?> token = this.tokens.get(tokenType);
		return (token != null) ? (Token<T>) token : null;
	}

	/**
	 * Returns the {@link Token} matching the {@code tokenValue}.
	 * @param tokenValue the token value
	 * @param <T> the type of the token
	 * @return the {@link Token}, or {@code null} if not available
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends OAuth2Token> Token<T> getToken(String tokenValue) {
		Assert.hasText(tokenValue, "tokenValue cannot be empty");
		for (Token<?> token : this.tokens.values()) {
			if (token.getToken().getTokenValue().equals(tokenValue)) {
				return (Token<T>) token;
			}
		}
		return null;
	}

	/**
	 * Returns the attribute(s) associated to the authorization.
	 * @return a {@code Map} of the attribute(s)
	 */
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	/**
	 * Returns the value of an attribute associated to the authorization.
	 * @param name the name of the attribute
	 * @param <T> the type of the attribute
	 * @return the value of an attribute associated to the authorization, or {@code null}
	 * if not available
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		Assert.hasText(name, "name cannot be empty");
		return (T) this.attributes.get(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		OAuth2Authorization that = (OAuth2Authorization) obj;
		return Objects.equals(this.id, that.id) && Objects.equals(this.registeredClientId, that.registeredClientId)
				&& Objects.equals(this.principalName, that.principalName)
				&& Objects.equals(this.authorizationGrantType, that.authorizationGrantType)
				&& Objects.equals(this.authorizedScopes, that.authorizedScopes)
				&& Objects.equals(this.tokens, that.tokens) && Objects.equals(this.attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.registeredClientId, this.principalName, this.authorizationGrantType,
				this.authorizedScopes, this.tokens, this.attributes);
	}

	/**
	 * Returns a new {@link Builder}, initialized with the provided
	 * {@link RegisteredClient#getId()}.
	 * @param registeredClient the {@link RegisteredClient}
	 * @return the {@link Builder}
	 */
	public static Builder withRegisteredClient(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		return new Builder(registeredClient.getId());
	}

	/**
	 * Returns a new {@link Builder}, initialized with the values from the provided
	 * {@code OAuth2Authorization}.
	 * @param authorization the {@code OAuth2Authorization} used for initializing the
	 * {@link Builder}
	 * @return the {@link Builder}
	 */
	public static Builder from(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		return new Builder(authorization.getRegisteredClientId()).id(authorization.getId())
			.principalName(authorization.getPrincipalName())
			.authorizationGrantType(authorization.getAuthorizationGrantType())
			.authorizedScopes(authorization.getAuthorizedScopes())
			.tokens(authorization.tokens)
			.attributes((attrs) -> attrs.putAll(authorization.getAttributes()));
	}

	/**
	 * A holder of an OAuth 2.0 Token and it's associated metadata.
	 *
	 * @param <T> the type of the {@link OAuth2Token}
	 * @author Joe Grandja
	 * @since 0.1.0
	 */
	public static class Token<T extends OAuth2Token> implements Serializable {

		@Serial
		private static final long serialVersionUID = -5931125502413497522L;

		protected static final String TOKEN_METADATA_NAMESPACE = "metadata.token.";

		/**
		 * The name of the metadata that indicates if the token has been invalidated.
		 */
		public static final String INVALIDATED_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("invalidated");

		/**
		 * The name of the metadata used for the claims of the token.
		 */
		public static final String CLAIMS_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("claims");

		private final T token;

		private final Map<String, Object> metadata;

		protected Token(T token) {
			this(token, defaultMetadata());
		}

		protected Token(T token, Map<String, Object> metadata) {
			this.token = token;
			this.metadata = Collections.unmodifiableMap(metadata);
		}

		/**
		 * Returns the token of type {@link OAuth2Token}.
		 * @return the token of type {@link OAuth2Token}
		 */
		public T getToken() {
			return this.token;
		}

		/**
		 * Returns {@code true} if the token has been invalidated (e.g. revoked). The
		 * default is {@code false}.
		 * @return {@code true} if the token has been invalidated, {@code false} otherwise
		 */
		public boolean isInvalidated() {
			return Boolean.TRUE.equals(getMetadata(INVALIDATED_METADATA_NAME));
		}

		/**
		 * Returns {@code true} if the token has expired.
		 * @return {@code true} if the token has expired, {@code false} otherwise
		 */
		public boolean isExpired() {
			return getToken().getExpiresAt() != null && Instant.now().isAfter(getToken().getExpiresAt());
		}

		/**
		 * Returns {@code true} if the token is before the time it can be used.
		 * @return {@code true} if the token is before the time it can be used,
		 * {@code false} otherwise
		 */
		public boolean isBeforeUse() {
			Instant notBefore = null;
			if (!CollectionUtils.isEmpty(getClaims())) {
				notBefore = (Instant) getClaims().get("nbf");
			}
			return notBefore != null && Instant.now().isBefore(notBefore);
		}

		/**
		 * Returns {@code true} if the token is currently active.
		 * @return {@code true} if the token is currently active, {@code false} otherwise
		 */
		public boolean isActive() {
			return !isInvalidated() && !isExpired() && !isBeforeUse();
		}

		/**
		 * Returns the claims associated to the token.
		 * @return a {@code Map} of the claims, or {@code null} if not available
		 */
		@Nullable
		public Map<String, Object> getClaims() {
			return getMetadata(CLAIMS_METADATA_NAME);
		}

		/**
		 * Returns the value of the metadata associated to the token.
		 * @param name the name of the metadata
		 * @param <V> the value type of the metadata
		 * @return the value of the metadata, or {@code null} if not available
		 */
		@Nullable
		@SuppressWarnings("unchecked")
		public <V> V getMetadata(String name) {
			Assert.hasText(name, "name cannot be empty");
			return (V) this.metadata.get(name);
		}

		/**
		 * Returns the metadata associated to the token.
		 * @return a {@code Map} of the metadata
		 */
		public Map<String, Object> getMetadata() {
			return this.metadata;
		}

		protected static Map<String, Object> defaultMetadata() {
			Map<String, Object> metadata = new HashMap<>();
			metadata.put(INVALIDATED_METADATA_NAME, false);
			return metadata;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Token<?> that = (Token<?>) obj;
			return Objects.equals(this.token, that.token) && Objects.equals(this.metadata, that.metadata);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.token, this.metadata);
		}

	}

	/**
	 * A builder for {@link OAuth2Authorization}.
	 */
	public static class Builder {

		private String id;

		private final String registeredClientId;

		private String principalName;

		private AuthorizationGrantType authorizationGrantType;

		private Set<String> authorizedScopes;

		private Map<Class<? extends OAuth2Token>, Token<?>> tokens = new HashMap<>();

		private final Map<String, Object> attributes = new HashMap<>();

		protected Builder(String registeredClientId) {
			this.registeredClientId = registeredClientId;
		}

		/**
		 * Sets the identifier for the authorization.
		 * @param id the identifier for the authorization
		 * @return the {@link Builder}
		 */
		public Builder id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets the {@code Principal} name of the resource owner (or client).
		 * @param principalName the {@code Principal} name of the resource owner (or
		 * client)
		 * @return the {@link Builder}
		 */
		public Builder principalName(String principalName) {
			this.principalName = principalName;
			return this;
		}

		/**
		 * Sets the {@link AuthorizationGrantType authorization grant type} used for the
		 * authorization.
		 * @param authorizationGrantType the {@link AuthorizationGrantType}
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantType(AuthorizationGrantType authorizationGrantType) {
			this.authorizationGrantType = authorizationGrantType;
			return this;
		}

		/**
		 * Sets the authorized scope(s).
		 * @param authorizedScopes the {@code Set} of authorized scope(s)
		 * @return the {@link Builder}
		 * @since 0.4.0
		 */
		public Builder authorizedScopes(Set<String> authorizedScopes) {
			this.authorizedScopes = authorizedScopes;
			return this;
		}

		/**
		 * Sets the {@link OAuth2AccessToken access token}.
		 * @param accessToken the {@link OAuth2AccessToken}
		 * @return the {@link Builder}
		 */
		public Builder accessToken(OAuth2AccessToken accessToken) {
			return token(accessToken);
		}

		/**
		 * Sets the {@link OAuth2RefreshToken refresh token}.
		 * @param refreshToken the {@link OAuth2RefreshToken}
		 * @return the {@link Builder}
		 */
		public Builder refreshToken(OAuth2RefreshToken refreshToken) {
			return token(refreshToken);
		}

		/**
		 * Sets the {@link OAuth2Token token}.
		 * @param token the token
		 * @param <T> the type of the token
		 * @return the {@link Builder}
		 */
		public <T extends OAuth2Token> Builder token(T token) {
			return token(token, (metadata) -> {
			});
		}

		/**
		 * Sets the {@link OAuth2Token token} and associated metadata.
		 * @param token the token
		 * @param metadataConsumer a {@code Consumer} of the metadata {@code Map}
		 * @param <T> the type of the token
		 * @return the {@link Builder}
		 */
		public <T extends OAuth2Token> Builder token(T token, Consumer<Map<String, Object>> metadataConsumer) {
			Assert.notNull(token, "token cannot be null");
			Map<String, Object> metadata = Token.defaultMetadata();
			Token<?> existingToken = this.tokens.get(token.getClass());
			if (existingToken != null) {
				metadata.putAll(existingToken.getMetadata());
			}
			metadataConsumer.accept(metadata);
			Class<? extends OAuth2Token> tokenClass = token.getClass();
			this.tokens.put(tokenClass, new Token<>(token, metadata));
			return this;
		}

		/**
		 * Invalidates the {@link OAuth2Token token}.
		 * @param token the token
		 * @param <T> the type of the token
		 * @return the {@link Builder}
		 * @since 1.4
		 */
		public <T extends OAuth2Token> Builder invalidate(T token) {
			Assert.notNull(token, "token cannot be null");
			if (this.tokens.get(token.getClass()) == null) {
				return this;
			}
			token(token, (metadata) -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
			if (OAuth2RefreshToken.class.isAssignableFrom(token.getClass())) {
				Token<?> accessToken = this.tokens.get(OAuth2AccessToken.class);
				token(accessToken.getToken(),
						(metadata) -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));

				Token<?> authorizationCode = this.tokens.get(OAuth2AuthorizationCode.class);
				if (authorizationCode != null && !authorizationCode.isInvalidated()) {
					token(authorizationCode.getToken(),
							(metadata) -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
				}
			}
			return this;
		}

		protected final Builder tokens(Map<Class<? extends OAuth2Token>, Token<?>> tokens) {
			this.tokens = new HashMap<>(tokens);
			return this;
		}

		/**
		 * Adds an attribute associated to the authorization.
		 * @param name the name of the attribute
		 * @param value the value of the attribute
		 * @return the {@link Builder}
		 */
		public Builder attribute(String name, Object value) {
			Assert.hasText(name, "name cannot be empty");
			Assert.notNull(value, "value cannot be null");
			this.attributes.put(name, value);
			return this;
		}

		/**
		 * A {@code Consumer} of the attributes {@code Map} allowing the ability to add,
		 * replace, or remove.
		 * @param attributesConsumer a {@link Consumer} of the attributes {@code Map}
		 * @return the {@link Builder}
		 */
		public Builder attributes(Consumer<Map<String, Object>> attributesConsumer) {
			attributesConsumer.accept(this.attributes);
			return this;
		}

		/**
		 * Builds a new {@link OAuth2Authorization}.
		 * @return the {@link OAuth2Authorization}
		 */
		public OAuth2Authorization build() {
			Assert.hasText(this.principalName, "principalName cannot be empty");
			Assert.notNull(this.authorizationGrantType, "authorizationGrantType cannot be null");

			OAuth2Authorization authorization = new OAuth2Authorization();
			if (!StringUtils.hasText(this.id)) {
				this.id = UUID.randomUUID().toString();
			}
			authorization.id = this.id;
			authorization.registeredClientId = this.registeredClientId;
			authorization.principalName = this.principalName;
			authorization.authorizationGrantType = this.authorizationGrantType;
			authorization.authorizedScopes = Collections.unmodifiableSet(!CollectionUtils.isEmpty(this.authorizedScopes)
					? new HashSet<>(this.authorizedScopes) : new HashSet<>());
			authorization.tokens = Collections.unmodifiableMap(this.tokens);
			authorization.attributes = Collections.unmodifiableMap(this.attributes);
			return authorization;
		}

	}

}
