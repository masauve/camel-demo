/*

kamel run --dev --profile=openshift --open-api=creditservice-openapi.yaml --config secret:my-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true creditservice.java

kamel run --profile=openshift --open-api=creditservice-openapi.yaml --config secret:my-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t istio.enabled=true -t knative.enabled=false creditservice.java
*/


public class creditservice extends org.apache.camel.builder.RouteBuilder {
  @Override
  public void configure() throws Exception {
        from("direct:writedb")
            .unmarshal().json()
            .log("BODY: ${body}")
            .setBody().simple("insert into transaction (CLIENT_ID, TYPE, LOCATION, AMOUNT) values ('${body[clientId]}', '${body[type]}','${body[location]}','${body[amount]}' );")
            .to("jdbc:camel")
            .setBody().simple("Success!").marshal().json();

        from("direct:getall")
        .log("Get all transactioins")
        .setBody().simple("select CLIENT_ID, TYPE, LOCATION, AMOUNT from transaction")
        .to("jdbc:camel").marshal().json();
 }
}
