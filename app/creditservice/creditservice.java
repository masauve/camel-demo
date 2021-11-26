/*

kamel run --dev --profile=openshift --open-api=creditservice-openapi.yaml --config secret:my-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true creditservice.java
*/


public class creditservice extends org.apache.camel.builder.RouteBuilder {
  @Override
  public void configure() throws Exception {
        from("direct:rest1")
            
            .log("BODY: ${body}")
           // .setBody().simple("insert into transaction (CLIENT_ID, TYPE, LOCATION, AMOUNT) values ('#${body[clientId]}', '${body.type}','${body.location}','${body.amount}' );")
            .to("stub:done");
        
 }
}