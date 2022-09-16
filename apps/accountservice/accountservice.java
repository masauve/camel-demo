/*
kamel run --dev --profile=openshift --open-api=accountservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true  -t istio.enabled=true transactionservice.java

kamel run --profile=openshift --open-api=accountservice-openapi.yaml --config secret:my-acccount-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false -t route.enabled=true -t istio.enabled=true transactionservice.java
*/

public class accountservice extends org.apache.camel.builder.RouteBuilder {

  @Override
  public void configure() throws Exception {
        from("direct:getAnAccount")
            .log("Get the account id for:  ${header.accountId}")
            .setBody().simple("select * from account where accountId = ${header.accountId}")
            .to("jdbc:camel").marshal().json();
  
        from("direct:createAccount")
            .unmarshal().json()
            .log("BODY: ${body}")
            .setBody().simple("insert into account (ACCOUNTID, ACCOUNT_TYPE, DISPLAYNAME, ACCOUNT_STATUS, ACCOUNT_DESCRIPTION, NICKNAME, CURRENCYCODE, INTERESTRATE, LOANTERM, TOTALNUMBEROFPAYMENTS, CURRENTBALANCE, AVAILABLEBALANCE  ) values ('${body[accountId]}', '${body[accountType]}','${body[displayName]}','${body[status]}','${body[description]}' , '${body[nickname]}', '${body[currencyCode]}','${body[interestRate]}', '${body[loanTerm]}', '${body[totalNumberOfPayments]}', '${body[currentBalance]}','${body[availableBalance]}');")
            .to("jdbc:camel")
            .setBody().simple("Success!").marshal().json();
 }
}
