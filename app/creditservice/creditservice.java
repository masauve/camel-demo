/*

kamel run --dev --config secret:my-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql:0.21.2.redhat-00005 -t route.enabled=true creditservice.java
*/


import org.apache.camel.builder.RouteBuilder;


public class creditservice extends RouteBuilder {
  @Override
  public void configure() throws Exception {

        rest()
            .consumes(MediaType.APPLICATION_JSON)
            .produces(MediaType.APPLICATION_JSON)
            .post("/credit")
            .route()
                .to("direct:writecredit")
            .endRest();

        from("direct:writecredit")
            .log("BODY: ${body}")
            .setBody(simple("insert into transaction (CLIENT_ID, TYPE, LOCATION, AMOUNT) values ('${body[clientId]}', '${body[type]}','${body[location]}','${body[amount]}' );"))
            .to("jdbc:camel")
            .setBody(simple("done!"));
        
 }