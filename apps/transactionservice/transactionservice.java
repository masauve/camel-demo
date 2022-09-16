/*
kamel run --dev --profile=openshift --open-api=transactionservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true  -t istio.enabled=true transactionservice.java

kamel run --profile=openshift --open-api=transactionservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true -t istio.enabled=true transactionservice.java
*/
import org.apache.camel.Exchange;
import org.apache.camel.AggregationStrategy;

public class transactionservice extends org.apache.camel.builder.RouteBuilder {

  @Override
  public void configure() throws Exception {
        from("direct:writetransaction")
        .removeHeader(Exchange.HTTP_URI)
        .removeHeader(Exchange.HTTP_PATH)
        .unmarshal().json()
        .log("request received: ${body}")
        .choice()
            .when().simple("${body[type]} == 'debit'")
                .to("direct:debitTransaction")
            .when().simple("${body[type]} == 'credit'")
                .to("direct:creditTransaction")
            .otherwise()
                .log( "invalid path : ${body[type]}" )
        .end();

        from("direct:readtransaction")
        .removeHeader(Exchange.HTTP_URI)
        .removeHeader(Exchange.HTTP_PATH)
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .multicast(new MyAggregationStrategy())
        .parallelProcessing().timeout(1000).to("direct:readcredit", "direct:readdebit")
        .end();
      
        from("direct:readcredit")
        .to("http:creditservice:80/credit?httpMethod=GET")
        .convertBodyTo(String.class);

        from("direct:readdebit")
        .doTry()
           .to("http:debitservice:80/debit?httpMethod=GET")
        .doCatch(Exception.class)
           .setBody().simple("{\"error\": \"Debit Service 403 forbidden\"}")
        .end()
        .convertBodyTo(String.class);

        from("direct:debitTransaction")
            .log("calling the debit service")
            .log("BODY: ${body}")
            .marshal().json()
            .to("http:debitservice:80/debit?httpMethod=POST");

        from("direct:creditTransaction")
            .log("calling the credit service")
            .log("BODY: ${body}")
            .marshal().json()
            .to("http:creditservice:80/credit?httpMethod=POST");

        from("direct:getaccount")
            .log("Get the account Id for ${header.accountId}")
            .setBody().simple("Success! - ${header.accountId}").marshal().json();   
  
        from("direct:createAccount")
            .unmarshal().json()
            .log("BODY: ${body}")
            .setBody().simple("insert into transaction (CLIENT_ID, TYPE, LOCATION, AMOUNT) values ('${body[clientId]}', '${body[type]}','${body[location]}','${body[amount]}' );")
            .to("jdbc:camel")
            .setBody().simple("Success!").marshal().json();

 }

 private class MyAggregationStrategy implements AggregationStrategy {
  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
      if (oldExchange == null) {
          return newExchange;
      }
      String newBody = newExchange.getIn().getBody(String.class);
      String oldBody = oldExchange.getIn().getBody(String.class);
      if(oldBody==null)oldBody="";
      if(newBody==null)newBody="";
      newBody = oldBody.concat("\n").concat(newBody);
      newExchange.getIn().setBody(newBody);
      return newExchange;
  }
}

}
