/*
 * DISCLAIMER
 * Copyright 2019 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 *
 */

package com.arangodb.graphql.spring.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.ArangoDefaults;

/**
 *
 * Initial implementation borrowed from the Arango spring-data Spring Boot Starter
 *
 * This version adds properties for GraphQL
 *
 * @author Mark Vollmary
 * @author Colin Findlay
 *
 */
@ConfigurationProperties(prefix = "arangodb")
public class ArangoGraphQLConfigurationProperties {

    /**
     * GraphQL Schema Location 
     */
    private String schemaLocation;

    /**
     * Database name.
     */
    private String database = "_system";

    /**
     * Hosts to connect to. Multiple hosts can be added to provide fallbacks in a
     * single server with active failover or load balancing in an cluster setup.
     */
    private Collection<String> hosts = new ArrayList<>();

    /**
     * Username to use for authentication.
     */
    private String user = ArangoDefaults.DEFAULT_USER;

    /**
     * Password for the user for authentication.
     */
    private String password;

    /**
     * Connection and request timeout in milliseconds.
     */
    private Integer timeout = ArangoDefaults.DEFAULT_TIMEOUT;

    /**
     * If set to {@code true} SSL will be used when connecting to an ArangoDB
     * server.
     */
    private Boolean useSsl = ArangoDefaults.DEFAULT_USE_SSL;

    /**
     * Maximum number of connections the built in connection pool will open per
     * host.
     */
    private Integer maxConnections = ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT;

    /**
     * Maximum time to life of a connection.
     */
    private Long connectionTtl;

    /**
     * Whether or not the driver should acquire a list of available coordinators in
     * an ArangoDB cluster or a single server with active failover.
     */
    private Boolean acquireHostList = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST;

    /**
     * Load balancing strategy to be used in an ArangoDB cluster setup.
     */
    private LoadBalancingStrategy loadBalancingStrategy = ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY;

    /**
     * Network protocol to be used to connect to ArangoDB.
     */
    private Protocol protocol = ArangoDefaults.DEFAULT_NETWORK_PROTOCOL;

    /**
     * Whether we should attempt to auto create ArangoDB databases, collections and indexes from the GraphQL schema
     */
    private boolean autoCreate;

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Collection<String> getHosts() {
        return hosts;
    }

    public void setHosts(final Collection<String> hosts) {
        this.hosts = hosts;
    }

    public  Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getUseSsl() {
        return useSsl;
    }

    public void setUseSsl(final Boolean useSsl) {
        this.useSsl = useSsl;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(final Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Long getConnectionTtl() {
        return connectionTtl;
    }

    public void setConnectionTtl(final Long connectionTtl) {
        this.connectionTtl = connectionTtl;
    }

    public Boolean getAcquireHostList() {
        return acquireHostList;
    }

    public void setAcquireHostList(final Boolean acquireHostList) {
        this.acquireHostList = acquireHostList;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public void setLoadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }


    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }
}