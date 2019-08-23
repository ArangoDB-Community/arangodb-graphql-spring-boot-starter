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

import com.arangodb.ArangoDB;
import com.arangodb.graphql.ArangoDataFetcher;
import com.arangodb.graphql.schema.runtime.ArangoRuntimeWiringBuilder;
import com.arangodb.graphql.schema.runtime.TypeDiscriminatorRegistry;
import com.arangodb.graphql.generator.ArangoQueryGeneratorChain;
import com.arangodb.graphql.generator.DefaultArangoQueryGeneratorChain;
import com.arangodb.graphql.query.ArangoTraversalQueryExecutor;
import com.arangodb.graphql.schema.ArangoDiscriminatorDirective;
import com.arangodb.graphql.schema.ArangoTypeAliasDirective;
import com.arangodb.graphql.schema.ArangoVertexDirective;
import com.arangodb.graphql.spring.ArangoGraphController;
import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.language.*;
import graphql.schema.*;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

/**
 * Auto Configuration for the GraphQL Components for ArangoDB
 *
 * @author Colin Findlay
 */
@Configuration
@EnableConfigurationProperties(ArangoGraphQLConfigurationProperties.class)
public class ArangoGraphQLAutoConfiguration {

    @Autowired
    private ArangoGraphQLConfigurationProperties properties;

    @Bean
    public ArangoDataFetcher arangoDataFetcher(ArangoQueryGeneratorChain queryGenerator, ArangoTraversalQueryExecutor queryExecutor){
        return new ArangoDataFetcher(queryGenerator, queryExecutor);
    }

    @Bean
    public ArangoQueryGeneratorChain arangoQueryGeneratorChain(){
        return new DefaultArangoQueryGeneratorChain();
    }

    @Bean
    public ArangoTraversalQueryExecutor arangoTraversalQueryExecutor(ArangoDB.Builder builder){
        return new ArangoTraversalQueryExecutor(builder.build(), properties.getDatabase());
    }

    @Bean
    public ArangoGraphController arangoGraphController(GraphQL graphQL) throws IOException {
        return new ArangoGraphController(graphQL);
    }

    @Bean
    public TypeDefinitionRegistry typeDefinitionRegistry() throws IOException {

        SchemaParser schemaParser = new SchemaParser();

        String schemaLocation = properties.getSchemaLocation();
        if(schemaLocation == null){
            schemaLocation = "*.graphqls";
        }

        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(schemaLocation);

        Set<Resource> allResources = new LinkedHashSet<>(resources.length+1);
        //  allResources.add(classPathResource);
        allResources.addAll(Arrays.asList(resources));

        for(Resource resource : allResources){
            typeRegistry.merge(schemaParser.parse(new InputStreamReader(resource.getInputStream())));
        }

        return typeRegistry;

    }

    @Bean
    public TypeDiscriminatorRegistry typeDiscriminatorRegistry(TypeDefinitionRegistry typeDefinitionRegistry){
        return new TypeDiscriminatorRegistry(typeDefinitionRegistry);
    }

    @Bean
    public GraphQL graphQL(ArangoDataFetcher fetcher, TypeDefinitionRegistry typeDefinitionRegistry, TypeDiscriminatorRegistry typeDiscriminatorRegistry){

        RuntimeWiring runtimeWiring = ArangoRuntimeWiringBuilder.newArangoRuntimeWiring(fetcher, typeDefinitionRegistry, typeDiscriminatorRegistry);

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();

    }

}
