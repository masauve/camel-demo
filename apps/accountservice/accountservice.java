/*
kamel run --dev --profile=openshift --open-api=accountservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true  -t istio.enabled=true transactionservice.java

kamel run --profile=openshift --open-api=accountservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true -t istio.enabled=true transactionservice.java
*/
import org.apache.camel.Exchange;
import org.apache.camel.AggregationStrategy;

public class accountservice extends org.apache.camel.builder.RouteBuilder {

  @Override
  public void configure() throws Exception {
        from("direct:getaccount")
            .log("Get the account Id for ${header.accountId}")
            .setBody().simple("Success! - ${header.accountId}").marshal().json();   
  
        from("direct:createAccount")
            .unmarshal().json()
            .log("BODY: ${body}")
            .setBody().simple("insert into account (CLIENT_ID, TYPE, LOCATION, AMOUNT) values ('${body[clientId]}', '${body[type]}','${body[location]}','${body[amount]}' );")
            .to("jdbc:camel")
            .setBody().simple("Success!").marshal().json();
 }
}
