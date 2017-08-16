connect('weblogic', 'weblogic1', "t3://127.0.0.1:7001")
domainConfig()
edit()
startEdit()

cmo.lookupAppDeployment('casual-jca-0.0.1').setDeploymentOrder(50)

save()
activate()
