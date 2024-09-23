#!/bin/bash
WORKING_DIR="$(dirname "${BASH_SOURCE[0]}")"
GLASSFISH_DIR=$WORKING_DIR/target/cargo/installs/glassfish-7.0.16/glassfish7/
echo $WORKING_DIR

if [[ ! -d $GLASSFISH_DIR ]];then
    echo "Installing Glassfish"
    mvn cargo:install
fi

mvn cargo:run &

$JAVA_HOME/bin/java -cp $GLASSFISH_DIR/glassfish/modules/glassfish.jar -DWALL_CLOCK_START=2024-08-19T22:08:15.207415Z -XX:+UnlockDiagnosticVMOptions -XX:NewRatio=2 -Xmx512m \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/sun.nio.fs=ALL-UNNAMED \
--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED \
--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED \
--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED \
--add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED \
--add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.vm.annotation=ALL-UNNAMED \
-javaagent:$GLASSFISH_DIR/glassfish/lib/monitor/flashlight-agent.jar \
-Djava.awt.headless=true \
-Djdk.corba.allowOutputStreamSubclass=true \
-Djdk.tls.rejectClientInitiatedRenegotiation=true \
-Djavax.xml.accessExternalSchema=all \
-Djava.security.policy=$WORKING_DIR/target/glassfish7x-home/cargo-domain/config/server.policy \
-Djava.security.auth.login.config=$WORKING_DIR/target/glassfish7x-home/cargo-domain/config/login.conf \
-Dcom.sun.enterprise.security.httpsOutboundKeyAlias=s1as \
-Djavax.net.ssl.keyStore=$WORKING_DIR/target/glassfish7x-home/cargo-domain/config/keystore.jks \
-Djavax.net.ssl.trustStore=$WORKING_DIR/target/glassfish7x-home/cargo-domain/config/cacerts.jks \
-Djdbc.drivers=org.apache.derby.jdbc.ClientDriver \
-DANTLR_USE_DIRECT_CLASS_LOADING=true \
-Dcom.sun.enterprise.config.config_environment_factory_class=com.sun.enterprise.config.serverbeans.AppserverConfigEnvironmentFactory \
-Dorg.glassfish.additionalOSGiBundlesToStart=org.apache.felix.shell,org.apache.felix.gogo.runtime,org.apache.felix.gogo.shell,org.apache.felix.gogo.command,org.apache.felix.shell.remote,org.apache.felix.fileinstall \
-Dosgi.shell.telnet.port=6666 -Dosgi.shell.telnet.maxconn=1 -Dosgi.shell.telnet.ip=127.0.0.1 \
-Dgosh.args=--nointeractive \
-Dfelix.fileinstall.dir=$GLASSFISH_DIR/glassfish/modules/autostart/ \
-Dfelix.fileinstall.poll=5000 \
-Dfelix.fileinstall.log.level=2 \
-Dfelix.fileinstall.bundles.new.start=true \
-Dfelix.fileinstall.bundles.startTransient=true \
-Dfelix.fileinstall.disableConfigSave=false \
-Dorg.glassfish.gmbal.no.multipleUpperBoundsException=true \
-Dcom.ctc.wstx.returnNullForDefaultNamespace=true \
-Djdk.attach.allowAttachSelf=true \
-Dcom.sun.aas.instanceRoot=$WORKING_DIR/target/glassfish7x-home/cargo-domain \
-Dcom.sun.aas.installRoot=$GLASSFISH_DIR/glassfish \
-Djava.library.path=$GLASSFISH_DIR/glassfish/lib:/usr/lib/java:$WORKING_DIR/target/glassfish7x-home com.sun.enterprise.glassfish.bootstrap.ASMain \
-upgrade false -domaindir $WORKING_DIR/target/glassfish7x-home/cargo-domain \
-read-stdin true -asadmin-args --host,,,localhost,,,--port,,,4848,,,--secure=false,,,--terse=false,,,--echo=false,,,--interactive=false,,,start-domain,,,--verbose=false,,,--watchdog=false,,,--debug=false,,,--domaindir,,,$WORKING_DIR/target/glassfish7x-home,,,cargo-domain -domainname cargo-domain -instancename server \
-type DAS -verbose false -asadmin-classpath $GLASSFISH_DIR/glassfish/modules/admin-cli.jar -debug false -asadmin-classname com.sun.enterprise.admin.cli.AdminMain
