oc create -f - <<EOF
kind: ServiceMeshMember
apiVersion: maistra.io/v1
metadata:
  name: default
  namespace: `oc config view --minify -o 'jsonpath={..namespace}'`
spec:
  controlPlaneRef:
    name: basic
    namespace: istio-system
EOF