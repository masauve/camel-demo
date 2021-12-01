domain=`oc get ingresses.config.openshift.io/cluster -o jsonpath={.spec.domain}`
namespace=`oc project -q`

host="${namespace}.istio-system.${domain}"

oc create -f - <<EOF
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: local-gateway
spec:
  selector:
    istio: ingressgateway # use istio default controller
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - ${host}
EOF
