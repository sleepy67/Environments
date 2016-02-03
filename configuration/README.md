mvn -f gen_config.pom.xml package -Dconfig.env=[LOCAL|CI|PROD] -Dconfig.tmpl.gid=[*] -Dconfig.tmpl.aid=[*] -Dconfig.tmpl.version=[*|LATEST|RELEASE](TFC-ProxyServices Version)

EG: TFC-ProxyServices::Configuration - LOCAL SNAPSHOT with specific version

mvn -f gen_config.pom.xml package -Dconfig.env=LOCAL -Dconfig.tmpl.gid=net.atos.tfc -Dconfig.tmpl.aid=Deployment -Dconfig.tmpl.version=0.0.1-SNAPSHOT

EG: TFC-ProxyServices::Configuration - LOCAL with LATEST version (will find the latest SNAPSHOT version)

mvn -f gen_config.pom.xml package -Dconfig.env=LOCAL -Dconfig.tmpl.gid=net.atos.tfc -Dconfig.tmpl.aid=Deployment -Dconfig.tmpl.version=LATEST

EG: TFC-ProxyServices::Configuration - PROD with RELEASE version (will find the latest RELEASE version)

mvn -f gen_config.pom.xml package -Dconfig.env=PROD -Dconfig.tmpl.gid=net.atos.tfc -Dconfig.tmpl.aid=Deployment -Dconfig.tmpl.version=RELEASE
