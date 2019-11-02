package com.arangodb.graphql.spring.boot;

import com.arangodb.graphql.create.DatabaseObjectCreator;
import com.arangodb.graphql.spring.boot.autoconfigure.AutomaticDatabaseObjectCreator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ActiveProfiles("city")
@SpringBootTest
public class DatabaseDisableAutoCreateTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void autoCreateDisabled() {
        applicationContext.getBean(AutomaticDatabaseObjectCreator.class);
    }
}
