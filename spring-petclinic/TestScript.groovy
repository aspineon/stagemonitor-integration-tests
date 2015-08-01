import groovy.json.JsonSlurper

// give stagemonitor some time to process the alerts
sleep 2000

// alerting
def incidents = new JsonSlurper().parse("http://localhost:8080/petclinic/stagemonitor/incidents".toURL())
assert incidents.status == "WARN"
assert incidents.incidents.first().checkName == "Response Time"

// disable stagemonitor via configuration
String activateUrl = "http://localhost:8080/petclinic/stagemonitor/configuration?configurationSource=Transient+Configuration+Source&stagemonitor.password=abc&key=stagemonitor.active&value="
post("${activateUrl}false")
assert !"http://localhost:8080/petclinic".toURL().text.contains('<iframe id="stagemonitor-modal"')
post("${activateUrl}true")

String post(String url) {
	URLConnection connection = url.toURL().openConnection()
	connection.setRequestMethod("POST")
	return connection.inputStream.withReader { it.text }
}