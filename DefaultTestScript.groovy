loadFallbackPropertiesIfExecutedDirectly("spring-petclinic")

def page = (props.testUrl ?: props.baseUrl).toURL().text

// MeasurementSession
assert parseJson(getJavascriptVariable(page, "measurementSession")).applicationName == props.applicationName

// iframe
assert page.contains('<iframe id="stagemonitor-modal"'): "The page does not contain the stagemonitor iframe"

// Profiler
def requestTrace = getJavascriptVariable(page, "data")
props.requestTraceContains.split(/\s*,\s*/).each {
	assert requestTrace.contains(it): "The request trace does not contain '$it'"
}

// Metrics
def metrics = parseJson("${props.baseUrl}/stagemonitor/metrics".toURL())
assert metrics?.find { it.name == "cpu_info_mhz"}?.values?.value >= 1
assert metrics?.find { it.name == "jvm_memory_heap" && it.tags?.type == "init"}?.values?.value >= 1
assert metrics?.find { it.name == "response_time" && it.tags?.operation_name == "All" }?.values?.count >= 1

// FileServlet
assert "${props.baseUrl}/stagemonitor/public/static/rum/boomerang.js".toURL().text.contains("var test;")

String getJavascriptVariable(String htmlPage, String variableName) {
	def m = htmlPage =~ /$variableName = (.*)[,;]/
	assert m.find(): "The page does not contain a request trace"
	return m.group(1)
}

def loadFallbackPropertiesIfExecutedDirectly(String testProject) {
	try {
		props
	} catch (MissingPropertyException e) {
		props = new Properties()
		new File("$testProject/stagemonitor-integration-test.properties").withInputStream {
			props.load(it)
		}
	}
}

def parseJson(json) {
	if (json instanceof URL) {
		return new groovy.json.JsonSlurper().parseText(json.text)
	}
	return new groovy.json.JsonSlurper().parseText(json as String)
}
