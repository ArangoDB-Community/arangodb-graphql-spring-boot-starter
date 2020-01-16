# Introduction

The ArangoDB GraphQL Spring Boot starter can be added to a Spring Boot application to add a GraphQL query interface to 
your application with ease and have it query your Arango Database.

# Getting Started

To get started, first add the starter to your project
```xml
<dependency>
  <groupId>com.arangodb</groupId>
  <artifactId>arangodb-graphql-spring-boot-starter</artifactId>
  <version>1.1</version>
</dependency>
```


Create a new GraphQL schema called `schema.graphqls` in your `src/main/resources` directory.

In your new GraphQL schema, declare the following special directives at the start of your file

```graphql schema
directive @edge(collection : String!, direction: String!) on FIELD_DEFINITION
directive @vertex(collection : String!) on OBJECT
directive @discriminator(property : String) on INTERFACE | UNION
directive @alias(name : String) on OBJECT
directive @edgeTarget on FIELD_DEFINITION
```

These directives will be used to add metadata to your GraphQL schema to help map your GraphQL types to your underlying 
physical data model in ArangoDB.

# Requirements

Currently, Java 8 is required. We plan to support newer Java versions in the future.

The current implementation uses the sync driver for ArangoDB. We also plan to support the
async driver in the future.

# Building and Testing

## Build the Libraries from scratch

To get started you can build the root POM in this directory with 

`mvn install`

## Set up your ArangoDB database

In ArangoDB - you can create any of the Example Graphs documented here:
https://www.arangodb.com/docs/3.4/graphs.html#example-graphs

## Configure your ArangoDB Connection Details

Edit the `/src/main/resources/application.yaml` file to
have your connection details in the arangodb section, as shown [here](/src/test/resources/application.yaml).

## Run your example GraphQL Service

In the [test resources](/src/test/resources), there is a Spring profile to match each of the example graphs. 
The profile names are:
- city
- knows
- mps
- social
- traversal
- world

# Example

Let's walk through a simple example with a simple Graph. Our Graph contains two entities Owners and Cars. Owners and 
Cars are linked by an edge indicating ownership.

Owners --> Cars

In Arango we have created this as an Owner document collection, a Car document collection and an ownership edge 
collection.

In GraphQL schema, we represent the two document collections as types, with a @vertex directive to indicate what 
collection it lives in.

## Create a GraphQL Schema

We can represent the properties of the document in each collection on each type, and we can represent the edge by 
declaring a property and adding the @edge directive to indicate where the edges live that represent that property.

```graphql schema
type Owner @vertex(collection: "Owners") {
    _id: String!
    name: String!
    cars: [Car] @edge(collection: "ownership", direction: "outbound")
}

type Car @vertex(collection: "Cars") {
    _id: String!
    make: String!
    model: String!
}
```

We can now create our Query operations in our schema. Suppose we as a query operation to look up an Owner by the name 
property of the document it would look like this. 

```graphql schema
type Query {

    findOwner(name: String): [Owner]
 
}
```

## Connecting to ArangoDB

In order to connect to ArangoDB - you must provide the following configuration properties in your application.yaml file.

```yaml
arangodb:
  hosts: <host>:<port>
  user: <user>
  password: <password>
  database: <database-name>
```

You can also auto create your database, collections and indexes from your GraphQL schema by enabling the following 
property in the application.yaml file

```yaml
arangodb:
  autoCreate: true
```


## Adding graphiql

To add GraphiQL to your project to give you a web UI to submit queries - simply add the following to you POM.

```XML
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphiql-spring-boot-starter</artifactId>
    <version>5.0.2</version>
</dependency>
``` 

## Running the example

If we run this example we could submit the following GraphQL Query, to get owners names `Colin` and the makes and models 
of their cars.

```graphql
query {
 findOwner(name: "Colin"){
  name
  cars {
    make,
    model
    }
  }
}
```
If you execute this query with a sample database you will see the following responses.

```json
{
  "data": {
    "findOwner": [
      {
        "name": "Colin",
        "cars": [
          {
            "make": "BMW",
            "model": "3 SERIES"
          },
          {
            "make": "MERCEDES-BENZ",
            "model": "GLE"
          }
        ]
      }
    ]
  }
}
```

## Bi-directional Traversal 

Let's expand on this example to navigate the graph in the opposite direction. Let suppose I want to find out who owns a 
particular make and model of car. To do this I would need to add another query operation to allow me to look up cars by 
Make and Model

```graphql schema
type Query {

    findOwner(name: String): [Owner]
    findCar(make: String, model: String): [Car]
 
}
```

I would also need to adjust my definition of the Car type so that I can navigate the edge in reverse. I do this by 
adding an owners property to the Car type and adding the @edge directive to indicate I should look for inbound edges 
from the ownership collection

```graphql schema
type Car @vertex(collection: "Cars") {
    _id: String!
    make: String!
    model: String!
    owners: [Owner] @edge(collection: "ownership", direction: "inbound")
}
```

Now we are able to query for say Owners of a BMW 3 Series

```graphql
query {
 findCar(make: "BMW", model: "3 SERIES"){
    make
    model
  	owners {
      name
    }
  }
}

```
Where the result would be as below. We can see that Robert & Colin both are owners of a BMW 3 Series.

```json
{
  "data": {
    "findCar": [
      {
        "make": "BMW",
        "model": "3 SERIES",
        "owners": [
          {
            "name": "Robert"
          }
        ]
      },
      {
        "make": "BMW",
        "model": "3 SERIES",
        "owners": [
          {
            "name": "Colin"
          }
        ]
      }
    ]
  }
}
```

As we now have a bi-directional relationship in our query we can now query both ways via GraphQL.

If I want to see who owns a BMW 3 Series, and what other cars they have I can traverse from the Cars to the Owners and 
back to the Cars those Owner own.

```graphql
query {
 findCar(make: "BMW", model: "3 SERIES"){
    make
    model
  	owners {
      name
      cars {
        make,
        model
      }
    }
  }
}
```

Which give us the below result, where I can see Colin also has a Mercedes GLE.

```json
{
  "data": {
    "findCar": [
      {
        "make": "BMW",
        "model": "3 SERIES",
        "owners": [
          {
            "name": "Colin",
            "cars": [
              {
                "make": "BMW",
                "model": "3 SERIES"
              },
              {
                "make": "MERCEDES-BENZ",
                "model": "GLE"
              }
            ]
          }
        ]
      },
      {
        "make": "BMW",
        "model": "3 SERIES",
        "owners": [
          {
            "name": "Robert",
            "cars": [
              {
                "make": "BMW",
                "model": "3 SERIES"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

## Filtering

The ArangoDB GraphQL library allows you to apply filtering on any field in your schema by adding optional arguments to 
the field. 

To quote the GraphQL documentation:

> If the only thing we could do was traverse objects and their fields, GraphQL would already be a very useful language 
for data fetching. But when you add the ability to pass arguments to fields, things get much more interesting.
In a system like REST, you can only pass a single set of arguments - the query parameters and URL segments in your 
request. But in GraphQL, every field and nested object can get its own set of arguments, making GraphQL a complete 
replacement for making multiple API fetches.

This library will automatically convert the arguments to `FILTER` statements in an AQL query to allow for filtering to 
occur in the ArangoDB. The filters that you can apply are completely controlled by the content of the GraphQL Schema.

Let's walk through an example.

Suppose we want to filter the cars an owner has by fuel type. To do that we can add a fuel field to our Car type, to 
allow us to see the fuel attribute of car documents in the Cars collection. We then add a `fuel` argument to the cars 
property of the Owner type to allow us to filter the cars by fuel type.

```graphql schema
type Owner @vertex(collection: "Owners") {
    _id: String!
    name: String!
    cars(fuel: String): [Car] @edge(collection: "ownership", direction: "outbound")
}

type Car @vertex(collection: "Cars") {
    _id: String!
    make: String!
    model: String!
    fuel: String!
    owners: [Owner] @edge(collection: "ownership", direction: "inbound")
}
```

This will allow us to specify an argument on the cars property of an Owner type in our query 

```graphql
query {
 findOwner {
  name
  cars (fuel: "DIESEL") {
    make,
    model
    fuel
    }
  }
}
```

Which would give us the following result as Colin is the only owner of diesel cars.

```json
{
  "data": {
    "findOwner": [
      {
        "name": "Colin",
        "cars": [
          {
            "make": "BMW",
            "model": "3 SERIES",
            "fuel": "DIESEL"
          },
          {
            "make": "MERCEDES-BENZ",
            "model": "GLE",
            "fuel": "DIESEL"
          }
        ]
      }
    ]
  }
}
```

### Multiple Filters

If you add multiple arguments to the field these will each become an AQL `FILTER` statement and as such constitute a 
logical `AND`. For example adding make, model and fuel.

```graphql schema
type Owner @vertex(collection: "Owners") {
    _id: String!
    name: String!
    cars(make: String, model: String, fuel: String): [Car] @edge(collection: "ownership", direction: "outbound")
}
```

Would allow us to specify multiple arguments. In the example below, we specify a make and a fuel which would match cars
that were Mercedes-Benz Diesels. You will also notice that although we could also specify a model, the argument is 
declared as optional in the schema - and as such is optional in the query. As it is not provided, we won't filter models.

```graphql
query {
 findOwner {
  name
  cars (make: "MERCEDES-BENZ", fuel: "DIESEL") {
    make,
    model
    fuel
    }
  }
}
```

## Complex Edges

You may wish to include information on an Edge document in ArangoDB, and have that be made available via your GraphQL 
interface

In our example, lets assume that our edge documents in the ownership collection have a property called `finance` which 
may have a value of 

- HP (Hire Purchase)
- PCP (Personal Contract Purchase)
- PCH (Personal Contract Hire)

In our example so far we have not exposed this property via our GraphQL interface. There are two ways to do this which 
are detailed below.

### Automatic Merge

In this scenario the properties on the edge document are automatically merged with the target document. To do this we 
simply add the property that is on the edge document to the target type. In this example we add a `finance` property to 
the car type. 

```graphql schema
type Car @vertex(collection: "Cars") {
    _id: String!
    make: String!
    model: String!
    fuel: String!
    finance: String
    owners: [Owner] @edge(collection: "ownership", direction: "inbound")
}
```

When we traverse from the Owner --> Car via the ownership edge, the `finance` property from the ownership
edge will be automatically merged into the Car instance. In the case of a conflict where an edge and a target have the 
same property, the target value always takes precedence.

The limitation of this approach is if we traverse to a Car not using the ownership edge or access it directly the 
property value will always be null. 

So for example if we use the `findCar` operation we defined earlier - this access the type directly, not via a traversal
so the `finance` property will always be null. 

This approach is a good choice if 

- You only access the type via a single type of edge
- You do not access the type directly
- You only traverse the edge leading to this type in an outbound direction

For example the following query will traverse from Owner --> Car via the Ownership edge and as a result the finance
property will be populated from the edge.

```graphql
query {
 findOwner {
  name
  cars {
    make,
    model
    fuel
    finance
    }
  }
}
```

For example the following query will not traverse from Owner --> Car via the Ownership edge and as a result the finance
property will be null.

```graphql
query {
 findCar {
  make,
  model,
  finance
 }
}
```

In this scenario, automatic merging is a suboptimal solution because our Car type specifies a property that is not
consistently populated.

### Edge Target

In this scenario, you create an intermediate type in your GraphQL schema to represent the edge relationship.

Here we create an Ownership type, with the `finance` property from the edge document, and a special property for the 
target of the edge marked with the `@edgeTarget` directive to indicate the target of the edge should be placed here.

```graphql schema
type Ownership {
    finance: String
    car: Car @edgeTarget
}
```

We then need to adjust our `Owner` type to make the cars field return the `Ownership` intermediate type we just created. 

```graphql schema
type Owner @vertex(collection: "Owners") {
    _id: String!
    name: String!
    cars(make: String, model: String, fuel: String): [Ownership] @edge(collection: "ownership", direction: "outbound")
}
```

This has an impact on the GraphQL query - notice how the finance property is now a sibling to the car, rather than a 
child of it. This now means we can now access the Car type directly without it being polluted by properties that can
only be populated when the type is accessed in a certain way. 

```graphql
query {
 findOwner {
  name
  cars {
    finance
    car {
      make
      model
    }
  }
 }
}
```

This mechanism also supports bi-directional traversal, however because the target of the edge is different when you 
traverse in the opposite direction, you need a second intermediate type to represent the edge in the reverse direction.
Here we have created the `OwnedBy` type which has an `@edgeTarget` of type `Owner`

```graphql schema
type OwnedBy {
    finance: String
    owner: Owner @edgeTarget
}
```

We then need to adjust our `Car` type to make the owners field return the `OwnedBy` intermediate type we just created. 

```graphql schema
query {
  findCar {
    make
    model
    owners {
      finance
      owner{
        name
      }
    }
  }
}
```

## Type Discrimination

To help GraphQL detect the object type, you can add optional type discriminator metadata to your schema definitions. 

In order to deal with inheritance via interface and union types in GraphQL the Arango GraphQL Adapter implements two 
mechanisms to achieve type discrimination. 

### Collection Based Type Discrimination

The default option is to use Collection Based Type Discrimination. This makes an assumption that every Document 
collection you have in ArangoDB maps to one and only one type. 

In this first example using interfaces we see we have two collections

- StandardCars
- ConvertibleCars

Instances of `StandardCar` are in the `StandardCars` collection, instances of `ConvertibleCar` are in the `ConvertibleCars` collection.

```graphql schema
interface Car  {
    make: String!
    model: String!
    variant: String!
}

type StandardCar implements Car @vertex(collection: "StandardCars") {
    make: String!
    model: String!
    variant: String!
}

type ConvertibleCar implements Car @vertex(collection: "ConvertibleCars") {
    make: String!
    model: String!
    variant: String!
    roofType: String!
}
```

Alternatively you can achieve the same result with a union.

```graphql schema
type StandardCar implements Car @vertex(collection: "StandardCars") {
    make: String!
    model: String!
    variant: String!
}

type ConvertibleCar implements Car @vertex(collection: "ConvertibleCars") {
    make: String!
    model: String!
    variant: String!
    roofType: String!
}

union Car = StandardCar | ConvertibleCar
```

This is the default option because it requires no additional configuration to achieve, but it is not intended to 
influence your design choices for how you structure the data in your Arango database. For more control, and the ability 
to store multiple document types in the same collection you will need to use Property Based Type Discrimination. 


### Property Based Type Discrimination

With Property Based Type Discrimination we use a property on a document to determine what concrete type to use.

In the following example we have 

- A `Car` interface and 
- A `StandardCar` concrete type that implement the `Car` interface
- A `ConvertibleCar` concrete type that implement the `Car` interface

In order to use a property called `vehicleType` on instances of Vehicle to determine if they are a `StandardCar` or a `ConvertibleCar`
we add the `@discriminator` directive to the `Vehicle` interface declaration.

```graphql schema
interface Car  @vertex(collection: "Cars") @discriminator(property: "vehicleType") {
    make: String!
    model: String!
    variant: String!
}

type StandardCar implements Car @vertex(collection: "Cars") {
    make: String!
    model: String!
    variant: String!
}

type ConvertibleCar implements Car @vertex(collection: "Cars") {
    make: String!
    model: String!
    variant: String!
    roofType: String!
}
```

Or as a union

```graphql schema
type StandardCar implements Car @vertex(collection: "Cars") {
    make: String!
    model: String!
    variant: String!
}

type ConvertibleCar implements Car @vertex(collection: "Cars") {
    make: String!
    model: String!
    variant: String!
    roofType: String!
}

union Vehicle = StandardCar | ConvertibleCar
```

Now the following document would be typed as a StandardCar

```json
{
  "make": "BMW",
  "model": "1 SERIES",
  "variant": "M140i",
  "fuel": "PETROL",
  "vehicleType": "StandardCar"
}
```

And the following document would be classed as a ConvertibleCar

```json
{
  "make": "BMW",
  "model": "Z4",
  "variant": "M40i",
  "fuel": "PETROL",
  "vehicleType": "ConvertibleCar"
}
```

### Type Alias

You will notice on the above example that the values in the vehicleType property in the documents directly match the 
names of the types in your GraphQL schema. This however might not always be practical, and you may want to map 
different values in the document on to types in the schema.

For example - lets say that your document contained a fully qualified Java class name as they value you might have 
documents that look like this:

```json
{
  "make": "BMW",
  "model": "1 SERIES",
  "variant": "M140i",
  "fuel": "PETROL",
  "vehicleType": "com.example.model.StandardCar"
}
```

```json
{
  "make": "BMW",
  "model": "Z4",
  "variant": "M40i",
  "fuel": "PETROL",
  "vehicleType": "com.example.model.ConvertibleCar"
}
```

In order for the Java type name in the document to map to our schema, you will need to create a type alias in your 
schema using the `@alias` directive.

```graphql schema
interface Car  @vertex(collection: "Cars") @discriminator(property: "vehicleType") {
    make: String!
    model: String!
    variant: String!
}

type StandardCar implements Car @vertex(collection: "Cars") @alias(name: "com.example.model.StandardCar"){
    make: String!
    model: String!
    variant: String!
}

type ConvertibleCar implements Car @vertex(collection: "Cars") @alias(name: "com.example.model.ConvertibleCar"){
    make: String!
    model: String!
    variant: String!
    roofType: String!
}
```


## Test

To run the tests a running instance of ArangoDB is required, which can be started using docker:
```shell script
docker run -p 8529:8529 -d -e ARANGO_NO_AUTH=1 --name arangodb arangodb/arangodb:3.5.0
``` 
and the graph examples should be loaded. For example for the `city` profile, the following is required:
```shell script
docker exec arangodb arangosh --server.authentication=false --javascript.execute-string='require("@arangodb/graph-examples/example-graph.js").loadGraph("routeplanner")'
```
