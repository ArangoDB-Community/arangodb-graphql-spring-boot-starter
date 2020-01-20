package com.arangodb.graphql.spring.boot.autoconfigure;

import com.arangodb.ArangoDB;
import com.arangodb.graphql.create.DatabaseObjectCreator;
import graphql.GraphQL;

import javax.annotation.PostConstruct;

public class AutomaticDatabaseObjectCreator {

    private final DatabaseObjectCreator databaseObjectCreator;

    public AutomaticDatabaseObjectCreator(DatabaseObjectCreator databaseObjectCreator){
        this.databaseObjectCreator = databaseObjectCreator;
    }

    @PostConstruct
    public void init(){

        databaseObjectCreator.createDatabase();
        databaseObjectCreator.createCollections();
        databaseObjectCreator.createIndexes();

    }

}
