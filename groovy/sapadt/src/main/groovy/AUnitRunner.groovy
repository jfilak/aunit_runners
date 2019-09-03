package sapadt

import groovy.json.*

import org.apache.http.*
import org.apache.http.client.methods.*
import org.apache.http.client.protocol.*
import org.apache.http.entity.*
import org.apache.http.auth.*
import org.apache.http.client.utils.*
import org.apache.http.impl.client.*

class AUnitRunner {
    private URIBuilder baseUri
    private CloseableHttpClient client

    AUnitRunner(String scheme, String hostname, int port, String client, String user, String password) {
        this.baseUri = URIBuilder.newInstance()
            .setScheme(scheme)
            .setHost(hostname)
            .setPort(port)
            .addParameter("sap-client", client)
            .addParameter("saml2", "disabled")

        def credsProvider = new BasicCredentialsProvider()
        credsProvider.setCredentials(
            new AuthScope(hostname, port),
            new UsernamePasswordCredentials(user, password));

        def builder = HttpClientBuilder.create()
        builder.setDefaultCredentialsProvider(credsProvider)

        this.client = builder.build()
    }

    String executeForPackage(String packageName) {
        def cookieStore = new BasicCookieStore();
        def context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        def discovery = new HttpHead(baseUri.setPath("sap/bc/adt/core/discovery").build())
        discovery.addHeader("x-csrf-token", "Fetch")
        def response = client.execute(discovery, context)

        def post = new HttpPost(baseUri.setPath("sap/bc/adt/abapunit/testruns").build())
        post.addHeader("Content-Type", "application/vnd.sap.adt.abapunit.testruns.config.v4+xml")
        post.addHeader("Accept", "application/xml")
        post.addHeader("x-csrf-token", response.getFirstHeader("x-csrf-token").getValue())

        post.setEntity(new StringEntity('''<?xml version="1.0" encoding="UTF-8"?>
<aunit:runConfiguration xmlns:aunit="http://www.sap.com/adt/aunit">
  <external>
    <coverage active="false"/>
  </external>
  <options>
    <uriType value="semantic"/>
    <testDeterminationStrategy sameProgram="true" assignedTests="false" appendAssignedTestsPreview="true"/>
    <testRiskLevels harmless="true" dangerous="true" critical="true"/>
    <testDurations short="true" medium="true" long="true"/>
  </options>
  <adtcore:objectSets xmlns:adtcore="http://www.sap.com/adt/core">
    <objectSet kind="inclusive">
      <adtcore:objectReferences>
        <adtcore:objectReference adtcore:uri="%s"/>
      </adtcore:objectReferences>
    </objectSet>
  </adtcore:objectSets>
</aunit:runConfiguration>'''.format("/sap/bc/adt/packages/" + packageName)))

        response = client.execute(post, context)

        def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
        return bufferedReader.getText()
    }
}
