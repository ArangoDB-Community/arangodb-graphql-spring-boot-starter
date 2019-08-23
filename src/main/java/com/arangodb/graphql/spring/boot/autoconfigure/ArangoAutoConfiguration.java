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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;


/**
 * {@link EnableAutoConfiguration} class for ArangoDB
 *
 * Initial implementation borrowed from the Arango spring-data Spring Boot Starter
 * Adding Properties for GraphQL
 *
 * @author Mark Vollmary
 * @author Colin Findlay
 *
 */
@Configuration
@EnableConfigurationProperties(ArangoGraphQLConfigurationProperties.class)
public class ArangoAutoConfiguration {

    private final ArangoGraphQLConfigurationProperties properties;

    @Autowired
    public ArangoAutoConfiguration(final ArangoGraphQLConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ArangoDB.Builder arango() {
        final ArangoDB.Builder builder = new ArangoDB.Builder()
                .user(properties.getUser())
                .password(properties.getPassword())
                .timeout(properties.getTimeout())
                .useSsl(properties.getUseSsl())
                .maxConnections(properties.getMaxConnections())
                .connectionTtl(properties.getConnectionTtl())
                .acquireHostList(properties.getAcquireHostList())
                .loadBalancingStrategy(properties.getLoadBalancingStrategy())
                .useProtocol(properties.getProtocol());
        properties.getHosts().stream().map(this::parseHost)
                .forEach(host -> builder.host(host[0], Integer.valueOf(host[1])));
        return builder;
    }

    private String[] parseHost(final String host) {
        final String[] split = host.split(":");
        if (split.length != 2 || !split[1].matches("[0-9]+")) {
            throw new ArangoDBException(String.format(
                    "Could not load host '%s' from property-value spring.data.arangodb.hosts. Expected format ip:port,ip:port,...",
                    host));
        }
        return split;
    }

}