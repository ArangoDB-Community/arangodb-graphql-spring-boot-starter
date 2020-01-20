package com.arangodb.graphql.spring.boot;

import com.arangodb.graphql.create.DatabaseObjectCreator;
import com.arangodb.graphql.spring.boot.autoconfigure.AutomaticDatabaseObjectCreator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class DatabaseAutoCreateTest {

    @Configuration
    @Import(AutomaticDatabaseObjectCreator.class) // A @Component injected with ExampleService
    static class Config {
    }

    @MockBean
    private DatabaseObjectCreator databaseObjectCreator;

    @Test
    public void autoCreateInvoked(){
        verify(databaseObjectCreator).createDatabase();
        verify(databaseObjectCreator).createCollections();
        verify(databaseObjectCreator).createIndexes();;
    }
}
