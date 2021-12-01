/*

kamel run --dev --profile=openshift --open-api=transactionservice-openapi.yaml -t knative.enabled=false -t route.enabled=true  -t istio.enabled=true transactionservice.java

kamel run --profile=openshift --open-api=transactionservice-openapi.yaml -t knative.enabled=false -t route.enabled=true -t istio.enabled=true transactionservice.java
*/
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
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
